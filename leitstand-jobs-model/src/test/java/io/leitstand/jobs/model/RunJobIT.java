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
import static io.leitstand.jobs.service.JobSubmission.newJobSubmission;
import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static io.leitstand.jobs.service.TaskState.ACTIVE;
import static io.leitstand.jobs.service.TaskState.COMPLETED;
import static io.leitstand.jobs.service.TaskSubmission.newTaskSubmission;
import static io.leitstand.jobs.service.TaskTransitionSubmission.newTaskTransitionSubmission;
import static io.leitstand.security.auth.UserName.userName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.enterprise.event.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.leitstand.commons.db.DatabaseService;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Repository;
import io.leitstand.jobs.service.JobApplication;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobProgress;
import io.leitstand.jobs.service.JobSettings;
import io.leitstand.jobs.service.JobSubmission;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskName;
import io.leitstand.jobs.service.TaskSubmission;
import io.leitstand.jobs.service.TaskTransitionSubmission;
import io.leitstand.jobs.service.TaskType;
import io.leitstand.security.auth.UserName;

public class RunJobIT extends JobsIT{

	private static final TaskName START 	= TaskName.valueOf("start");
	private static final TaskName SPLIT 	= TaskName.valueOf("split");
	private static final TaskName BRANCH_A0 = TaskName.valueOf("branch_a_0");
	private static final TaskName BRANCH_A1 = TaskName.valueOf("branch_a_1");
	private static final TaskName BRANCH_B0 = TaskName.valueOf("branch_b_0");
	private static final TaskName JOIN		= TaskName.valueOf("join");
	private static final TaskName END		= TaskName.valueOf("end");
	
	private static final TaskType UNIT		= TaskType.valueOf("unit");
	
	private static final TaskSubmission task(TaskName taskName) {
		return newTaskSubmission()
			   .withTaskId(randomTaskId())
			   .withTaskName(taskName)
			   .withTaskType(UNIT)
			   .build();
	}
	
	private static final TaskTransitionSubmission transition(TaskSubmission from,
															 TaskSubmission to){
		return newTaskTransitionSubmission()
			   .from(from.getTaskId())
			   .to(to.getTaskId())
			   .build();
	}
	
	private static final void assertSuccessors(List<TaskId> successors, TaskSubmission... expected) {
		assertEquals("Mismatch on expected successors",expected.length,successors.size());
		for(TaskSubmission task : expected) {
			assertTrue(successors.contains(task.getTaskId()));
		}
	}
	
	private DefaultJobService jobs;
	private DefaultJobTaskService tasks;
	private JobId jobId;
	private Event<TaskStateChangedEvent> event;
	private TaskProcessor processor;
	private TaskSubmission start; 	
	private TaskSubmission split; 	
	private TaskSubmission branchA0; 
	private TaskSubmission branchA1; 
	private TaskSubmission branchB0; 
	private TaskSubmission join;		
	private TaskSubmission end; 		
	
	@Before
	public void create_job() {
		// Create repository to test DB interaction
		Repository repository = new Repository(getEntityManager());

		// Create job service and IT job definition.
		jobs = new DefaultJobService(repository, 
									 mock(DatabaseService.class),
									 mock(InventoryClient.class),
									 new JobEditor(repository),
									 mock(Messages.class),
									 userName("dummy"));

		jobId = randomJobId();
		start 	 = task(START);
	 	split 	 = task(SPLIT);
	 	branchA0 = task(BRANCH_A0);
	 	branchA1 = task(BRANCH_A1);
	 	branchB0 = task(BRANCH_B0);
	 	join	 = task(JOIN);
	 	end 	 = task(END);
		
		JobSubmission job = newJobSubmission()
						    .withJobApplication(JobApplication.valueOf("IntegrationTest"))
							.withTasks(start,
									   split,
									   branchA0,
									   branchA1,
									   branchB0,
									   join,
									   end)
							.withTransitions(transition(start,split),
											 transition(split,branchA0),
											 transition(branchA0,branchA1),
									 		 transition(branchA1,join),
									 		 transition(split,branchB0),
									 		 transition(branchB0,join),
									 		 transition(join,end))
							.build();
		
		beginTransaction();
		// Store job
		jobs.storeJob(jobId, 
					  job);
		// Set job eligible for deployment
		jobs.commitJob(jobId);
		commitTransaction();
		event = mock(Event.class);
		processor = mock(TaskProcessor.class);
		TaskProcessorDiscoveryService discovery = mock(TaskProcessorDiscoveryService.class);
		when(discovery.findElementTaskProcessor(any(Job_Task.class))).thenReturn(processor);
		tasks = new DefaultJobTaskService(repository,
										  new TaskProcessingService(discovery),
										  event);
		
	}
	
	
	@Test
	public void run_job_synchronously() {
		when(processor.execute(any(Job_Task.class))).thenReturn(COMPLETED);
		beginTransaction();
		List<TaskId> successors = tasks.executeTask(jobId,start.getTaskId());
		assertSuccessors(successors, split);
		commitTransaction();

		beginTransaction();
		successors = tasks.executeTask(jobId,split.getTaskId());
		assertSuccessors(successors, branchA0,branchB0);
		commitTransaction();

		beginTransaction();
		successors = tasks.executeTask(jobId,branchA0.getTaskId());
		assertSuccessors(successors, branchA1);
		commitTransaction();

		beginTransaction();
		successors = tasks.executeTask(jobId,branchA1.getTaskId());
		assertTrue(successors.isEmpty());
		commitTransaction();
		
		beginTransaction();
		successors = tasks.executeTask(jobId,branchB0.getTaskId());
		assertSuccessors(successors, join);
		commitTransaction();
		
		beginTransaction();
		successors = tasks.executeTask(jobId,join.getTaskId());
		assertSuccessors(successors, end);
		commitTransaction();
		
		beginTransaction();
		successors = tasks.executeTask(jobId,end.getTaskId());
		assertTrue(successors.isEmpty());
		commitTransaction();
		
	}
	
	@Test
	public void run_job_asynchronously() {
		when(processor.execute(any(Job_Task.class))).thenReturn(ACTIVE);
		
		// Execute start task asynchronously
		beginTransaction();
		List<TaskId> successors = tasks.executeTask(jobId,start.getTaskId());
		assertTrue(successors.isEmpty());
		commitTransaction();
		
		// Complete start task
		beginTransaction();
		successors = tasks.updateTask(jobId,start.getTaskId(), COMPLETED);
		assertSuccessors(successors, split);
		commitTransaction();

		// Execute split task asynchronously
		beginTransaction();
		successors = tasks.executeTask(jobId,split.getTaskId());
		assertTrue(successors.isEmpty());
		commitTransaction();
		
		// Complete split task
		beginTransaction();
		successors = tasks.updateTask(jobId,split.getTaskId(), COMPLETED);
		assertSuccessors(successors, branchA0,branchB0);
		commitTransaction();
		
		// Execute branchA0 task asynchronously
		beginTransaction();
		successors = tasks.executeTask(jobId,branchA0.getTaskId());
		assertTrue(successors.isEmpty());
		commitTransaction();
		
		// Complete branchA0 task
		beginTransaction();
		successors = tasks.updateTask(jobId,branchA0.getTaskId(), COMPLETED);
		assertSuccessors(successors, branchA1);
		commitTransaction();

		// Execute branchA1 task asynchronously
		beginTransaction();
		successors = tasks.executeTask(jobId,branchA1.getTaskId());
		assertTrue(successors.isEmpty());
		commitTransaction();
		
		// Complete branchA1 task
		beginTransaction();
		successors = tasks.updateTask(jobId,branchA1.getTaskId(), COMPLETED);
		assertTrue(successors.isEmpty());
		commitTransaction();

		// Execute branchB0 task asynchronously
		beginTransaction();
		successors = tasks.executeTask(jobId,branchB0.getTaskId());
		assertTrue(successors.isEmpty());
		commitTransaction();
		
		// Complete branchB0 task
		beginTransaction();
		successors = tasks.updateTask(jobId,branchB0.getTaskId(), COMPLETED);
		assertSuccessors(successors, join);
		commitTransaction();
		
		// Execute join task asynchronously
		beginTransaction();
		successors = tasks.executeTask(jobId,join.getTaskId());
		assertTrue(successors.isEmpty());
		commitTransaction();
		
		// Complete branchA0 task
		beginTransaction();
		successors = tasks.updateTask(jobId,join.getTaskId(), COMPLETED);
		assertSuccessors(successors, end);
		commitTransaction();
		
		// Execute end task asynchronously
		beginTransaction();
		successors = tasks.executeTask(jobId,end.getTaskId());
		assertTrue(successors.isEmpty());
		commitTransaction();
		
		// Complete end task
		beginTransaction();
		successors = tasks.updateTask(jobId,end.getTaskId(), COMPLETED);
		assertTrue(successors.isEmpty());
		commitTransaction();
		
	}
	
	@After
	public void verify_job_completed() {
		JobProgress progress = jobs.getJobProgress(jobId);
		assertEquals(0,progress.getActiveCount());
		assertEquals(0,progress.getFailedCount());
		assertEquals(0,progress.getReadyCount());
		assertEquals(7,progress.getCompletedCount());
		JobSettings settings = jobs.getJobSettings(jobId);
		assertEquals(COMPLETED,settings.getJobState());
		
	}
	

	
}
