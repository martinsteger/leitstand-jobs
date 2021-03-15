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

import static io.leitstand.inventory.service.ElementAlias.elementAlias;
import static io.leitstand.inventory.service.ElementGroupId.randomGroupId;
import static io.leitstand.inventory.service.ElementGroupName.groupName;
import static io.leitstand.inventory.service.ElementGroupType.groupType;
import static io.leitstand.inventory.service.ElementId.randomElementId;
import static io.leitstand.inventory.service.ElementName.elementName;
import static io.leitstand.inventory.service.ElementRoleName.elementRoleName;
import static io.leitstand.inventory.service.ElementSettings.newElementSettings;
import static io.leitstand.jobs.service.JobApplication.jobApplication;
import static io.leitstand.jobs.service.JobId.randomJobId;
import static io.leitstand.jobs.service.JobName.jobName;
import static io.leitstand.jobs.service.JobType.jobType;
import static io.leitstand.jobs.service.ReasonCode.JOB0200E_TASK_NOT_FOUND;
import static io.leitstand.jobs.service.ReasonCode.JOB0203E_TASK_OWNED_BY_OTHER_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0204E_CANNOT_MODIFY_TASK_OF_RUNNING_JOB;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static io.leitstand.jobs.service.TaskName.taskName;
import static io.leitstand.jobs.service.TaskType.taskType;
import static io.leitstand.testing.ut.LeitstandCoreMatchers.reason;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.json.JsonObject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.ConflictException;
import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.Repository;
import io.leitstand.inventory.service.ElementSettings;
import io.leitstand.jobs.service.JobApplication;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobName;
import io.leitstand.jobs.service.JobTaskInfo;
import io.leitstand.jobs.service.JobType;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskName;
import io.leitstand.jobs.service.TaskType;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJobTaskServiceTest {
	
	private static final TaskId TASK_ID = randomTaskId();
	private static final TaskName TASK_NAME = taskName("task");
	private static final TaskType TASK_TYPE = taskType("type");
	
	private static final JobId JOB_ID = randomJobId();
	private static final JobName JOB_NAME = jobName("job");
	private static final JobType JOB_TYPE = jobType("type");
	private static final JobApplication JOB_APP = jobApplication("app");
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Mock
	private Repository repository;
	
	@Mock
	private InventoryClient inventory;
	
	@Mock
	private JobProvider jobs;
	
	@Mock
	private TaskProcessingService processor;
	
	@Mock
	private Messages messages;
	
	@InjectMocks
	private DefaultJobTaskService service = new DefaultJobTaskService();
	
	@Test
	public void update_task_throws_EntityNotFoundException_when_task_does_not_exist() {
        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn(JOB_ID);
        when(jobs.fetchJob(JOB_ID)).thenReturn(job);

	    
	    exception.expect(EntityNotFoundException.class);
	    exception.expect(reason(JOB0200E_TASK_NOT_FOUND));
	    
	    service.updateTask(JOB_ID, TASK_ID, COMPLETED);
	}
	
	@Test
	public void excute_task_throws_EntityNotFoundException_when_task_does_not_exist() {
        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn(JOB_ID);
        when(jobs.fetchJob(JOB_ID)).thenReturn(job);

	    exception.expect(EntityNotFoundException.class);
	    exception.expect(reason(JOB0200E_TASK_NOT_FOUND));
	        
	    service.executeTask(JOB_ID, TASK_ID);
	}
	
	@Test
	public void update_task() {
	    Job job = mock(Job.class);
	    Job_Task task = mock(Job_Task.class);
	    when(job.getJobId()).thenReturn(JOB_ID);
	    when(job.getTask(TASK_ID)).thenReturn(task);
	    when(jobs.fetchJob(JOB_ID)).thenReturn(job);
	    
	    service.updateTask(JOB_ID, TASK_ID, COMPLETED);
	    verify(processor).updateTask(task, COMPLETED);
	}
	
    @Test
    public void execute_task() {
        Job job = mock(Job.class);
        Job_Task task = mock(Job_Task.class);
        when(job.getJobId()).thenReturn(JOB_ID);
        when(job.getTask(TASK_ID)).thenReturn(task);
        when(jobs.fetchJob(JOB_ID)).thenReturn(job);
        
        service.executeTask(JOB_ID, TASK_ID);
        verify(processor).executeTask(task);
    }
	
    @Test
    public void get_job_task_throws_EntityNotFoundException_for_unknown_task_id() {
        exception.expect(EntityNotFoundException.class);
        exception.expect(reason(JOB0200E_TASK_NOT_FOUND));
        
        service.getJobTask(JOB_ID, TASK_ID);
    }
    
    @Test
    public void get_job_task_throws_ConflictException_when_task_is_owned_by_other_job() {
        exception.expect(ConflictException.class);
        exception.expect(reason(JOB0203E_TASK_OWNED_BY_OTHER_JOB));
        
        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn(randomJobId());
        Job_Task task = mock(Job_Task.class);
        when(task.getJob()).thenReturn(job);
        when(repository.execute(any(Query.class))).thenReturn(task);
        
        service.getJobTask(JOB_ID, TASK_ID);
    }
	
    @Test
    public void get_job_task() {
        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn(JOB_ID);
        when(job.getJobName()).thenReturn(JOB_NAME);
        when(job.getJobType()).thenReturn(JOB_TYPE);
        when(job.getJobApplication()).thenReturn(JOB_APP);
        Job_Task task = mock(Job_Task.class);
        when(task.getTaskId()).thenReturn(TASK_ID);
        when(task.getTaskName()).thenReturn(TASK_NAME);
        when(task.getTaskType()).thenReturn(TASK_TYPE);
        when(task.getTaskState()).thenReturn(COMPLETED);
        when(task.getJobId()).thenReturn(JOB_ID);
        when(task.getJob()).thenReturn(job);
        when(task.getDateModified()).thenReturn(new Date());
        when(repository.execute(any(Query.class))).thenReturn(task);
        
        JobTaskInfo jobTask = service.getJobTask(JOB_ID, TASK_ID);
        
        assertEquals(JOB_ID, jobTask.getJobId());
        assertEquals(JOB_APP, jobTask.getJobApplication());
        assertEquals(JOB_TYPE, jobTask.getJobType());
        assertEquals(JOB_NAME, jobTask.getJobName());
        assertEquals(TASK_ID, jobTask.getTaskId());
        assertEquals(TASK_NAME, jobTask.getTaskName());
        assertEquals(TASK_TYPE, jobTask.getTaskType());
        assertEquals(COMPLETED, jobTask.getTaskState());
        assertNull(jobTask.getGroupId());
        assertNull(jobTask.getGroupType());
        assertNull(jobTask.getGroupName());
        assertNull(jobTask.getElementId());
        assertNull(jobTask.getElementName());
        assertNull(jobTask.getElementAlias());
        assertNull(jobTask.getElementRole());
        
    }
    
    @Test
    public void get_element_job_task() {
        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn(JOB_ID);
        when(job.getJobName()).thenReturn(JOB_NAME);
        when(job.getJobType()).thenReturn(JOB_TYPE);
        when(job.getJobApplication()).thenReturn(JOB_APP);
        Job_Task task = mock(Job_Task.class);
        when(task.getTaskId()).thenReturn(TASK_ID);
        when(task.getTaskName()).thenReturn(TASK_NAME);
        when(task.getTaskType()).thenReturn(TASK_TYPE);
        when(task.getTaskState()).thenReturn(COMPLETED);
        when(task.getJobId()).thenReturn(JOB_ID);
        when(task.getDateModified()).thenReturn(new Date());
        when(task.getJob()).thenReturn(job);
        when(repository.execute(any(Query.class))).thenReturn(task);
        
        ElementSettings element = newElementSettings()
                                  .withGroupId(randomGroupId())
                                  .withGroupType(groupType("type"))
                                  .withGroupName(groupName("group"))
                                  .withElementId(randomElementId())
                                  .withElementName(elementName("element"))
                                  .withElementAlias(elementAlias("role"))
                                  .withElementRole(elementRoleName("role"))
                                  .build();
        
        when(inventory.getElementSettings(task)).thenReturn(element);
        
        JobTaskInfo jobTask = service.getJobTask(JOB_ID, TASK_ID);
        
        assertEquals(JOB_ID, jobTask.getJobId());
        assertEquals(JOB_APP, jobTask.getJobApplication());
        assertEquals(JOB_TYPE, jobTask.getJobType());
        assertEquals(JOB_NAME, jobTask.getJobName());
        assertEquals(TASK_ID, jobTask.getTaskId());
        assertEquals(TASK_NAME, jobTask.getTaskName());
        assertEquals(TASK_TYPE, jobTask.getTaskType());
        assertEquals(COMPLETED, jobTask.getTaskState());
        assertEquals(element.getGroupId(),jobTask.getGroupId());
        assertEquals(element.getGroupType(),jobTask.getGroupType());
        assertEquals(element.getGroupName(),jobTask.getGroupName());
        assertEquals(element.getElementId(),jobTask.getElementId());
        assertEquals(element.getElementName(),jobTask.getElementName());
        assertEquals(element.getElementAlias(),jobTask.getElementAlias());
        assertEquals(element.getElementRole(),jobTask.getElementRole());
        
    }
    
    @Test
    public void set_task_parameter_throws_EntityNotFoundException_when_task_does_not_exist() {
        exception.expect(EntityNotFoundException.class);
        exception.expect(reason(JOB0200E_TASK_NOT_FOUND));
        
        service.setTaskParameter(JOB_ID, 
                                 TASK_ID, 
                                 mock(JsonObject.class));
    }
    
    @Test
    public void set_task_parameter_throws_ConflictException_when_task_is_owned_by_other_job() {
        exception.expect(ConflictException.class);
        exception.expect(reason(JOB0203E_TASK_OWNED_BY_OTHER_JOB));
        
        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn(randomJobId());
        Job_Task task = mock(Job_Task.class);
        when(task.getJob()).thenReturn(job);
        when(repository.execute(any(Query.class))).thenReturn(task);
        
        service.setTaskParameter(JOB_ID, 
                                 TASK_ID,
                                 mock(JsonObject.class));
    }
    
    @Test
    public void cannot_modify_task_parameters_of_running_jobs() {
        exception.expect(ConflictException.class);
        exception.expect(reason(JOB0204E_CANNOT_MODIFY_TASK_OF_RUNNING_JOB));
        
        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn(JOB_ID);
        when(job.isRunning()).thenReturn(true);
        Job_Task task = mock(Job_Task.class);
        when(task.getJobId()).thenReturn(JOB_ID);
        when(task.getJob()).thenReturn(job);
        when(repository.execute(any(Query.class))).thenReturn(task);
        
        
        service.setTaskParameter(JOB_ID, 
                                 TASK_ID,
                                 mock(JsonObject.class));
    }
    
    @Test
    public void cannot_modify_task_parameters_of_completed_task() {
        exception.expect(ConflictException.class);
        exception.expect(reason(JOB0204E_CANNOT_MODIFY_TASK_OF_RUNNING_JOB));
        
        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn(JOB_ID);
        when(job.isRunning()).thenReturn(true);
        Job_Task task = mock(Job_Task.class);
        when(task.getJobId()).thenReturn(JOB_ID);
        when(task.getJob()).thenReturn(job);
        when(task.isSucceeded()).thenReturn(true);
        when(repository.execute(any(Query.class))).thenReturn(task);
        
        
        service.setTaskParameter(JOB_ID, 
                                 TASK_ID,
                                 mock(JsonObject.class));
    }
    
    @Test
    public void set_task_parameters_of_failed_task() {
        
        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn(JOB_ID);
        Job_Task task = mock(Job_Task.class);
        when(task.getJobId()).thenReturn(JOB_ID);
        when(task.getJob()).thenReturn(job);
        when(repository.execute(any(Query.class))).thenReturn(task);
        
        JsonObject parameter = mock(JsonObject.class);
        
        service.setTaskParameter(JOB_ID, 
                                 TASK_ID,
                                 parameter);
        
        verify(task).setParameter(parameter);
    }
    
}
