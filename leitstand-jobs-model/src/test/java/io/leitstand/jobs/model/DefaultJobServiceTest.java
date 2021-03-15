/*
 * Copyright 2020 RtBrick Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.leitstand.jobs.model;

import static io.leitstand.jobs.service.JobId.randomJobId;
import static io.leitstand.jobs.service.JobName.jobName;
import static io.leitstand.jobs.service.JobSchedule.newJobSchedule;
import static io.leitstand.jobs.service.JobSettings.newJobSettings;
import static io.leitstand.jobs.service.ReasonCode.JOB0101I_JOB_SETTINGS_UPDATED;
import static io.leitstand.jobs.service.ReasonCode.JOB0102E_JOB_SETTINGS_IMMUTABLE;
import static io.leitstand.jobs.service.ReasonCode.JOB0103I_JOB_CONFIRMED;
import static io.leitstand.jobs.service.ReasonCode.JOB0104I_JOB_CANCELLED;
import static io.leitstand.jobs.service.ReasonCode.JOB0105I_JOB_RESUMED;
import static io.leitstand.jobs.service.ReasonCode.JOB0106E_CANNOT_CANCEL_COMPLETED_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0107I_JOB_STORED;
import static io.leitstand.jobs.service.ReasonCode.JOB0108I_JOB_REMOVED;
import static io.leitstand.jobs.service.ReasonCode.JOB0109E_CANNOT_COMMIT_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0110E_CANNOT_RESUME_COMPLETED_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0111E_JOB_NOT_REMOVABLE;
import static io.leitstand.jobs.service.State.ACTIVE;
import static io.leitstand.jobs.service.State.CANCELLED;
import static io.leitstand.jobs.service.State.WAITING;
import static io.leitstand.security.auth.UserName.userName;
import static io.leitstand.testing.ut.LeitstandCoreMatchers.reason;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.ConflictException;
import io.leitstand.commons.db.DatabaseService;
import io.leitstand.commons.messages.Message;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Repository;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobSchedule;
import io.leitstand.jobs.service.JobSettings;
import io.leitstand.jobs.service.JobSubmission;
import io.leitstand.jobs.service.State;
import io.leitstand.security.auth.UserContext;
import io.leitstand.security.auth.UserName;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJobServiceTest {
    
    private static final JobId JOB_ID = randomJobId();
    
    private static final UserName AUTHENTICATED_USER = userName("unittest");
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
	
	@Mock
	private Repository repository;
	
	@Mock
	private Jobs jobs;
	
	@Mock
	private JobProvider provider;
	
	@Mock
	private InventoryClient inventory;
	
	@Mock
	private DatabaseService db;
	
	@Mock
	private JobEditor editor;
	
	@Mock
	private Messages messages;
	
	private ArgumentCaptor<Message> messageCaptor;
	
	@Mock
	private UserContext userContext;
	
	@InjectMocks
	private DefaultJobService service = new DefaultJobService();
	
	@Before
	public void initUserContext() {
		when(userContext.getUserName()).thenReturn(userName("junit"));
		messageCaptor = ArgumentCaptor.forClass(Message.class);
		doNothing().when(messages).add(messageCaptor.capture());
		when(userContext.getUserName()).thenReturn(AUTHENTICATED_USER);
	}
	
	@Test
	public void store_job_creates_new_job_if_no_job_with_specified_id_exists() {
		JobSubmission submission = mock(JobSubmission.class);
		ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		doNothing().when(editor).updateJob(jobCaptor.capture(),eq(submission));
		service.storeJob(JOB_ID,submission);
		assertEquals(JOB_ID,jobCaptor.getValue().getJobId());
		
	}
	
	@Test
	public void commit_job() {
        Job job = mock(Job.class);
        when(job.isNew()).thenReturn(true);
        when(provider.fetchJob(JOB_ID)).thenReturn(job);
		
	    service.commitJob(JOB_ID);
	    verify(job).setJobState(State.READY);
	    assertEquals(JOB0107I_JOB_STORED.getReasonCode(),
	                 messageCaptor.getValue().getReason());
	}

	@Test
	public void cannot_commit_committed_job() {
		Job job = mock(Job.class);
		when(provider.fetchJob(JOB_ID)).thenReturn(job);
		
		exception.expect(ConflictException.class);
		exception.expect(reason(JOB0109E_CANNOT_COMMIT_JOB));
		
		service.commitJob(JOB_ID);
	}
	
	@Test
	public void run_job_immediately_when_no_schedule_date_is_set() {
		Job job = mock(Job.class);
		when(job.isNew()).thenReturn(true);
		when(provider.fetchJob(JOB_ID)).thenReturn(job);
		ArgumentCaptor<Date> dateScheduled = ArgumentCaptor.forClass(Date.class);
		doNothing().when(job).setDateScheduled(dateScheduled.capture());
		service.commitJob(JOB_ID);
		
		verify(job).setDateScheduled(dateScheduled.getValue());
		assertEquals(JOB0107I_JOB_STORED.getReasonCode(),
		             messageCaptor.getValue().getReason());
	}
	
	@Test
	public void store_creates_new_job_when_job_not_exists() {
		ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		doNothing().when(repository).add(jobCaptor.capture());
		
		service.storeJob(randomJobId(),mock(JobSubmission.class));
		Job job = jobCaptor.getValue();
		assertNotNull(job);
		verify(repository).add(job);
		
	}

	@Test
	public void confirm_activates_suspended_job() {
	    Job job = mock(Job.class);
	    when(provider.fetchJob(JOB_ID)).thenReturn(job);
	    when(job.isSuspended()).thenReturn(true);
	    
	    service.confirmJob(JOB_ID);
	    verify(job).confirmed();
	    verify(job).completed();
	    assertEquals(JOB0103I_JOB_CONFIRMED.getReasonCode(),
	                 messageCaptor.getValue().getReason());
	}
	
    @Test
    public void confirm_does_nothing_when_job_is_not_suspended() {
        Job job = mock(Job.class);
        when(provider.fetchJob(JOB_ID)).thenReturn(job);
        
        service.confirmJob(JOB_ID);
        verify(job,never()).confirmed();
        verify(job,never()).completed();
    }
    
    @Test
    public void cannot_modify_settings_of_completed_job() {
        Job job = mock(Job.class);
        when(provider.fetchJob(JOB_ID)).thenReturn(job);
        when(job.isCompleted()).thenReturn(true);
        
        exception.expect(ConflictException.class);
        exception.expect(reason(JOB0102E_JOB_SETTINGS_IMMUTABLE));
        
        service.storeJobSettings(JOB_ID, mock(JobSettings.class));
    }
	    
    @Test
    public void cannot_modify_settings_of_running_job() {
        Job job = mock(Job.class);
        when(provider.fetchJob(JOB_ID)).thenReturn(job);
        when(job.isRunning()).thenReturn(true);
        
        exception.expect(ConflictException.class);
        exception.expect(reason(JOB0102E_JOB_SETTINGS_IMMUTABLE));
        
        service.storeJobSettings(JOB_ID, mock(JobSettings.class));
    }
    
    @Test
    public void update_settings_of_planned_job() {
        JobSchedule schedule = newJobSchedule()
                               .withStartTime(new Date())
                               .build();
        
        JobSettings settings = newJobSettings()
                               .withJobName(jobName("job"))
                               .withSchedule(schedule)
                               .build();
        
        Job job = mock(Job.class);
        when(provider.fetchJob(JOB_ID)).thenReturn(job);

        service.storeJobSettings(JOB_ID, settings);
        
        verify(job).setJobName(settings.getJobName());
        verify(job).setAutoResume(false);
        verify(job).setDateSuspend(null);
        verify(job).setDateScheduled(schedule.getDateScheduled());
        verify(job).setJobOwner(AUTHENTICATED_USER);
        
        assertEquals(JOB0101I_JOB_SETTINGS_UPDATED.getReasonCode(),
                     messageCaptor.getValue().getReason());
    }
    
    @Test
    public void remove_unknown_job_does_not_reoprt_an_error() {
        service.removeJob(JOB_ID);
        verifyZeroInteractions(messages,repository);
    }

    @Test
    public void cannot_remove_running_job() {
        Job job = mock(Job.class);
        when(provider.tryFetchJob(JOB_ID)).thenReturn(job);
        
        exception.expect(ConflictException.class);
        exception.expect(reason(JOB0111E_JOB_NOT_REMOVABLE));
      
        service.removeJob(JOB_ID);
        
    }
    
    @Test
    public void remove_terminated_job() {
        Job job = mock(Job.class);
        when(provider.tryFetchJob(JOB_ID)).thenReturn(job);
        when(job.isTerminated()).thenReturn(true);
        
        service.removeJob(JOB_ID);

        verify(repository).remove(job);
        assertEquals(JOB0108I_JOB_REMOVED.getReasonCode(),
                     messageCaptor.getValue().getReason());
        
    }
    
    @Test
    public void cannot_cancel_completed_job() {
        Job job = mock(Job.class);
        when(job.isCompleted()).thenReturn(true);
        when(provider.fetchJob(JOB_ID)).thenReturn(job);
        
        exception.expect(ConflictException.class);
        exception.expect(reason(JOB0106E_CANNOT_CANCEL_COMPLETED_JOB));
        
        service.cancelJob(JOB_ID);
    }
    
    @Test
    public void cancel_job() {
        Job job = mock(Job.class);
        
        Job_Task completed = mock(Job_Task.class);
        when(completed.isTerminated()).thenReturn(true);
        Job_Task active = mock(Job_Task.class);
        Job_Task ready = mock(Job_Task.class);
        when(job.getTaskList()).thenReturn(asList(completed,active,ready));
        
        when(provider.fetchJob(JOB_ID)).thenReturn(job);
        
        service.cancelJob(JOB_ID);
        
        verify(job).setJobState(CANCELLED);
        verify(completed,never()).setTaskState(CANCELLED);
        verify(ready).setTaskState(CANCELLED);
        verify(active).setTaskState(CANCELLED);
        assertEquals(JOB0104I_JOB_CANCELLED.getReasonCode(),
                     messageCaptor.getValue().getReason());
        
    }
    
    @Test
    public void cannot_resume_completed_job() {
        Job job = mock(Job.class);
        when(job.isCompleted()).thenReturn(true);
        when(provider.fetchJob(JOB_ID)).thenReturn(job);

        exception.expect(ConflictException.class);
        exception.expect(reason(JOB0110E_CANNOT_RESUME_COMPLETED_JOB));
        service.resumeJob(JOB_ID);
        
    }
    
    @Test
    public void resume_failed_job() {
        Job job = mock(Job.class);
        when(job.isFailed()).thenReturn(true);    
        Job_Task completed = mock(Job_Task.class);
        Job_Task failed = mock(Job_Task.class);
        when(failed.isResumable()).thenReturn(true);
        Job_Task ready = mock(Job_Task.class);
        when(job.getTaskList()).thenReturn(asList(completed,failed,ready));
        
        when(provider.fetchJob(JOB_ID)).thenReturn(job);
        
        service.resumeJob(JOB_ID);
        
        verify(job).setJobState(ACTIVE);
        verify(completed,never()).setTaskState(WAITING);
        verify(ready,never()).setTaskState(WAITING);
        verify(failed).setTaskState(WAITING);
        assertEquals(JOB0105I_JOB_RESUMED.getReasonCode(),
                     messageCaptor.getValue().getReason());
        
    }
    
    @Test
    public void resume_cancelled_job() {
        Job job = mock(Job.class);
        when(job.isCancelled()).thenReturn(true);    
        Job_Task completed = mock(Job_Task.class);
        Job_Task failed = mock(Job_Task.class);
        when(failed.isResumable()).thenReturn(true);
        Job_Task ready = mock(Job_Task.class);
        when(job.getTaskList()).thenReturn(asList(completed,failed,ready));
        
        when(provider.fetchJob(JOB_ID)).thenReturn(job);
        
        service.resumeJob(JOB_ID);
        
        verify(job).setJobState(ACTIVE);
        verify(completed,never()).setTaskState(any(State.class));
        verify(ready,never()).setTaskState(any(State.class));
        verify(failed).setTaskState(WAITING);
        assertEquals(JOB0105I_JOB_RESUMED.getReasonCode(),
                     messageCaptor.getValue().getReason());
        
    }
    
}
