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

import static io.leitstand.jobs.model.JobTaskMother.activeTask;
import static io.leitstand.jobs.model.JobTaskMother.completedTask;
import static io.leitstand.jobs.model.JobTaskMother.failedTask;
import static io.leitstand.jobs.model.JobTaskMother.readyTask;
import static io.leitstand.jobs.model.JobTaskMother.rejectedTask;
import static io.leitstand.jobs.service.JobApplication.jobApplication;
import static io.leitstand.jobs.service.JobId.randomJobId;
import static io.leitstand.jobs.service.JobName.jobName;
import static io.leitstand.jobs.service.JobType.jobType;
import static io.leitstand.jobs.service.State.ACTIVE;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.State.FAILED;
import static io.leitstand.jobs.service.State.READY;
import static io.leitstand.jobs.service.State.SKIPPED;
import static io.leitstand.jobs.service.State.WAITING;
import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static io.leitstand.security.auth.UserName.userName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import io.leitstand.jobs.service.State;

public class JobTest {
	
	private Job job;
	
	@Before
	public void prepareJob() {
		job = new Job(jobApplication("junit"),
				      jobType("test"),
				      randomJobId(),
				      jobName("test"),
				      userName("unittest"));
	}

	@Test
	public void cannot_complete_job_with_failed_tasks() {
		job.addTask(completedTask());
		job.addTask(failedTask());
		assertFalse(job.completed());
	}
	
	@Test
	public void cannot_complete_job_with_ready_tasks() {
		job.addTask(completedTask());
		job.addTask(readyTask());
		assertFalse(job.completed());
	}

	@Test
	public void cannot_complete_job_with_active_tasks() {
		job.addTask(completedTask());
		job.addTask(activeTask());
		assertFalse(job.completed());
	}
	
	@Test
	public void cannot_complete_job_with_rejected_tasks() {
		job.addTask(completedTask());
		job.addTask(rejectedTask());
		assertFalse(job.completed());
	}

	
	@Test
	public void can_complete_job_with_completed_tasks() {
		job.addTask(completedTask());
		job.addTask(completedTask());
		assertTrue(job.completed());
		assertEquals(COMPLETED,job.getJobState());
	}
	
	@Test
	public void submit_new_job() {
	    Job_Task a = mock(Job_Task.class);
	    when(a.getTaskId()).thenReturn(randomTaskId());
	    Job_Task b = mock(Job_Task.class);
        when(b.getTaskId()).thenReturn(randomTaskId());
	    Job_Task c = mock(Job_Task.class);
        when(c.getTaskId()).thenReturn(randomTaskId());

	    job.addTask(a);
	    job.addTask(b);
	    job.addTask(c);
	    job.submit();

	    assertEquals(READY,job.getJobState());
	    verify(a).setTaskState(WAITING);
        verify(b).setTaskState(WAITING);
        verify(c).setTaskState(WAITING);
	    
	}
	
	@Test
    public void do_nothing_when_submitting_submitted_job() {
        Job_Task a = mock(Job_Task.class);
        when(a.getTaskId()).thenReturn(randomTaskId());
        Job_Task b = mock(Job_Task.class);
        when(b.getTaskId()).thenReturn(randomTaskId());
        Job_Task c = mock(Job_Task.class);
        when(c.getTaskId()).thenReturn(randomTaskId());
        job.setJobState(State.READY);

        job.addTask(a);
        job.addTask(b);
        job.addTask(c);
        job.submit();

        assertEquals(READY,job.getJobState());
        verify(a,never()).setTaskState(READY);
        verify(b,never()).setTaskState(READY);
        verify(c,never()).setTaskState(READY);
        
    }

	@Test
	public void job_remains_in_confirm_state_when_unconfirmed_jobs_exist() {
	    Job_Task completed = mock(Job_Task.class);
	    Job_Task confirmed = mock(Job_Task.class);
	    when(confirmed.isSuspended()).thenReturn(true);
	    job.addTask(completed);
	    job.addTask(confirmed);
	    job.setJobState(State.CONFIRM);

	    
	    job.confirmed();
	    
	    assertEquals(State.CONFIRM,job.getJobState());
	}
	
    @Test
    public void job_state_changes_to_active_when_all_tasks_are_confirmed() {
        Job_Task a = mock(Job_Task.class);
        Job_Task b = mock(Job_Task.class);
        job.addTask(a);
        job.addTask(b);
        job.setJobState(State.CONFIRM);

        job.confirmed();
        
        assertEquals(ACTIVE,job.getJobState());
    }
    
    @Test
    public void mark_job_as_failed() {
        Job_Task completed = mock(Job_Task.class);
        Job_Task failed  = mock(Job_Task.class);
        Job_Task ready  = mock(Job_Task.class);
        when(ready.isReady()).thenReturn(true);

        job.addTask(completed);
        job.addTask(failed);
        job.addTask(ready);
        
        job.failed();
        
        assertEquals(FAILED,job.getJobState());
        verify(completed,never()).setTaskState(SKIPPED);
        verify(failed,never()).setTaskState(SKIPPED);
        verify(ready).setTaskState(SKIPPED);
        
    }
	
}
