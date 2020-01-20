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

import static io.leitstand.jobs.service.TaskState.ACTIVE;
import static io.leitstand.jobs.service.TaskState.COMPLETED;
import static io.leitstand.jobs.service.TaskState.CONFIRM;
import static io.leitstand.jobs.service.TaskState.FAILED;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.enterprise.event.Event;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.Repository;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.TaskId;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJobTaskServiceTest {
	
	private static final TaskId TASK_ID = TaskId.valueOf("JUNIT");
	private static final JobId JOB_ID = JobId.randomJobId();
	@Mock
	private Repository repository;
	
	@Mock
	private InventoryClient client;
	
	@Mock
	private Event event;
	
	@Mock
	private TaskProcessingService processor;
	
	@InjectMocks
	private DefaultJobTaskService service = new DefaultJobTaskService();
	
	
	private Job job;
	private Job_Task task;
	
	@Before
	public void initTestDoubles() {
		job = mock(Job.class);
		when(job.getJobId()).thenReturn(JOB_ID);
		task = mock(Job_Task.class);
		
		when(task.getTaskId()).thenReturn(TASK_ID);
		when(task.getJob()).thenReturn(job);
		when(job.getTask(TASK_ID)).thenReturn(task);
	}
	
	@Test
	public void set_job_to_CONFIRM_state_when_canary_task_is_completed() {
		when(repository.execute(any(Query.class))).thenReturn(job)
												  .thenReturn(task);
		when(task.isCanary()).thenReturn(TRUE);
		when(task.isActive()).thenReturn(TRUE);
		
		List<TaskId> successors = service.updateTask(JOB_ID,TASK_ID,COMPLETED);
		assertTrue(successors.isEmpty());
		verify(task).setTaskState(CONFIRM);
		verify(job).setJobState(CONFIRM);
		verify(event).fire(any(TaskStateChangedEvent.class));
		verifyZeroInteractions(client,
							   processor);
		
	}
	
	@Test
	public void ask_not_for_conformation_to_execute_canary_task() {
		when(repository.execute(any(Query.class))).thenReturn(job)
												  .thenReturn(task);
		when(task.isCanary()).thenReturn(TRUE);
		
		List<TaskId> successors = service.updateTask(JOB_ID,TASK_ID,ACTIVE);
		assertTrue(successors.isEmpty());
		verify(task).setTaskState(ACTIVE);
		verify(event).fire(any(TaskStateChangedEvent.class));
		verifyZeroInteractions(client,
							   processor);
	}
	
	
	
	@Test
	public void ask_not_for_conformation_if_canary_task_failed() {
		when(repository.execute(any(Query.class))).thenReturn(job)
												  .thenReturn(task);
		when(task.isCanary()).thenReturn(TRUE);
		when(task.isFailed()).thenReturn(TRUE);
		
		List<TaskId> successors = service.updateTask(JOB_ID,TASK_ID,FAILED);
		assertTrue(successors.isEmpty());
		verify(task).setTaskState(FAILED);
		verify(event).fire(any(TaskStateChangedEvent.class));
		verify(job).failed();
		verifyZeroInteractions(client,
							   processor);
	}
	
	
	@Test
	public void set_job_to_ACTIVE_state_when_canary_task_is_confirmed() {
		when(repository.execute(any(Query.class))).thenReturn(job)
												  .thenReturn(task);
		when(task.isCanary()).thenReturn(TRUE);
		when(task.isSuspended()).thenReturn(TRUE);
		Job_Task successor = mock(Job_Task.class);
		Job_Task_Transition transition = mock(Job_Task_Transition.class);
		when(transition.getTo()).thenReturn(successor);
		when(task.getSuccessors()).thenReturn(asList(transition));
		
		List<TaskId> successors = service.updateTask(JOB_ID,TASK_ID,COMPLETED);
		assertFalse(successors.isEmpty());
		verify(task).setCanary(false);
		verify(task).setTaskState(COMPLETED);
		verify(job).setJobState(ACTIVE);
		verify(event).fire(any(TaskStateChangedEvent.class));
		verifyZeroInteractions(client,
							   processor);
		
	}
	
	@Test
	public void attempt_to_complete_job_when_no_more_successors_exist() {
		when(repository.execute(any(Query.class))).thenReturn(job)
												  .thenReturn(task);
		List<TaskId> successors = service.updateTask(JOB_ID,TASK_ID,COMPLETED);
		assertTrue(successors.isEmpty());
		verify(task).setTaskState(COMPLETED);
		verify(job).completed();
		verify(event).fire(any(TaskStateChangedEvent.class));
		verifyZeroInteractions(client,
							   processor);
	}
	
	@Test
	public void mark_job_as_FAILED_when_task_is_failed() {
		when(repository.execute(any(Query.class))).thenReturn(job)
												  .thenReturn(task);
		when(task.isFailed()).thenReturn(TRUE);
		
		List<TaskId> successors = service.updateTask(JOB_ID,TASK_ID,FAILED);
		assertTrue(successors.isEmpty());
		verify(task).setTaskState(FAILED);
		verify(job).failed();
		verify(event).fire(any(TaskStateChangedEvent.class));
		verifyZeroInteractions(client,
							   processor);
	}
	
	@Test
	public void do_not_process_blocked_task() {
		when(repository.execute(any(Query.class))).thenReturn(job)
												  .thenReturn(task);

		List<TaskId> successors = service.executeTask(JOB_ID,TASK_ID);
		assertTrue(successors.isEmpty());
		verifyZeroInteractions(processor,
							   event);	
	}
	
	@Test
	public void attempt_to_complete_job_when_task_was_completed_synchronously_without_successors() {
		when(repository.execute(any(Query.class))).thenReturn(job)
												  .thenReturn(task);
		when(task.isReady()).thenReturn(TRUE);
		when(task.isTerminated()).thenReturn(TRUE);
		when(task.isSucceeded()).thenReturn(TRUE);
		when(processor.execute(task)).thenReturn(emptyList());
		List<TaskId> successors = service.executeTask(JOB_ID,TASK_ID);
		assertTrue(successors.isEmpty());
		verify(job).completed();
		verifyZeroInteractions(event);	
	}
	
	@Test
	public void do_not_attempt_to_complete_job_when_task_was_completed_synchronously_with_successors() {
		when(repository.execute(any(Query.class))).thenReturn(job)
												  .thenReturn(task);
		when(task.isReady()).thenReturn(TRUE);
		when(task.isTerminated()).thenReturn(TRUE);
		when(task.isSucceeded()).thenReturn(TRUE);
		Job_Task successor = mock(Job_Task.class);
		when(processor.execute(task)).thenReturn(asList(successor));

		
		List<TaskId> successors = service.executeTask(JOB_ID,TASK_ID);
		assertFalse(successors.isEmpty());
		verify(job,never()).completed();
		verifyZeroInteractions(event);	
	}
	
	@Test
	public void set_job_failed_when_task_was_completed_synchronously_and_failed() {
		when(repository.execute(any(Query.class))).thenReturn(job)
												  .thenReturn(task);
		when(task.isReady()).thenReturn(TRUE);
		when(task.isTerminated()).thenReturn(TRUE);
		when(task.isSucceeded()).thenReturn(FALSE);
		when(processor.execute(task)).thenReturn(emptyList());
		
		List<TaskId> successors = service.executeTask(JOB_ID,TASK_ID);
		assertTrue(successors.isEmpty());
		verify(job).failed();
		verifyZeroInteractions(event);	
	}
	
	
}
