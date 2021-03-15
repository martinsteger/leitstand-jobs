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

import static io.leitstand.jobs.model.Job_Task.findTaskById;
import static io.leitstand.jobs.model.TaskResult.active;
import static io.leitstand.jobs.model.TaskResult.completed;
import static io.leitstand.jobs.service.JobId.randomJobId;
import static io.leitstand.jobs.service.JobSubmission.newJobSubmission;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static io.leitstand.jobs.service.TaskSubmission.newTaskSubmission;
import static io.leitstand.jobs.service.TaskTransitionSubmission.newTaskTransitionSubmission;
import static io.leitstand.security.auth.UserName.userName;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.leitstand.commons.db.DatabaseService;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Repository;
import io.leitstand.jobs.service.JobApplication;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobProgress;
import io.leitstand.jobs.service.JobSubmission;
import io.leitstand.jobs.service.State;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskName;
import io.leitstand.jobs.service.TaskSubmission;
import io.leitstand.jobs.service.TaskTransitionSubmission;
import io.leitstand.jobs.service.TaskType;
import io.leitstand.security.auth.UserContext;

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
	private TaskProcessor processor;
	private TaskSubmission start; 	
	private TaskSubmission split; 	
	private TaskSubmission branchA0; 
	private TaskSubmission branchA1; 
	private TaskSubmission branchB0; 
	private TaskSubmission join;		
	private TaskSubmission end; 
	private Repository repository;
	
	@Before
	public void create_job() {
		// Create repository to test DB interaction
		repository = new Repository(getEntityManager());

		UserContext userContext = mock(UserContext.class);
		when(userContext.getUserName()).thenReturn(userName("dummy"));
		
		// Create job service and IT job definition.
		jobs = new DefaultJobService(repository, 
		                             new JobProvider(repository),
									 mock(DatabaseService.class),
									 mock(InventoryClient.class),
									 new JobEditor(repository),
									 mock(Messages.class),
									 userContext);

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
		
		transaction(() -> {
		// Store job
		jobs.storeJob(jobId, 
					  job);
		// Set job eligible for deployment
		jobs.commitJob(jobId);
		});
		processor = mock(TaskProcessor.class);
		TaskProcessorDiscoveryService discovery = mock(TaskProcessorDiscoveryService.class);
		when(discovery.findElementTaskProcessor(any(Job_Task.class))).thenReturn(processor);
		tasks = new DefaultJobTaskService(repository,
                                          new JobProvider(repository),
										  new TaskProcessingService(discovery));
		
	}
	
	List<TaskId> executeTask(JobId jobId, TaskId taskId){
	    tasks.executeTask(jobId,taskId);
	    return findSuccessorsEligbleForExecution(taskId);
	}

	List<TaskId> updateTask(JobId jobId, TaskId taskId, State state){
	    tasks.updateTask(jobId, taskId, state);
	    return findSuccessorsEligbleForExecution(taskId);
	}

	
    private List<TaskId> findSuccessorsEligbleForExecution(TaskId taskId) {
        Job_Task task = repository.execute(findTaskById(taskId));
	    
        return task.getSuccessors()
	               .stream()
	               .map(Job_Task_Transition::getTo)
	               .filter(Job_Task::isEligibleForExecution)
	               .map(Job_Task::getTaskId)
	               .collect(toList());
    }
	
	@Test
	public void run_job_synchronously() {
		when(processor.execute(any(Job_Task.class))).thenReturn(completed());
		transaction(()->{
		    List<TaskId> successors = executeTask(jobId,start.getTaskId());
		    assertSuccessors(successors, split);
		});

		transaction(() -> {
		    List<TaskId> successors = executeTask(jobId,split.getTaskId());
		    assertSuccessors(successors, branchA0,branchB0);
		});
		
		transaction(() -> {
		    List<TaskId >successors = executeTask(jobId,branchA0.getTaskId());
		    assertSuccessors(successors, branchA1);
		});

		transaction(() -> {
		    List<TaskId> successors = executeTask(jobId,branchA1.getTaskId());
		    assertTrue(successors.isEmpty());
	    });
		
		transaction(() -> {
		    List<TaskId> successors = executeTask(jobId,branchB0.getTaskId());
		    assertSuccessors(successors, join);
		});
		
		transaction(() -> {
		    List<TaskId >successors = executeTask(jobId,join.getTaskId());
		    assertSuccessors(successors, end);
		});
		
		transaction(() -> {
		    List<TaskId >successors = executeTask(jobId,end.getTaskId());
		    assertTrue(successors.isEmpty());
		});
	}
	
	@Test
	public void run_job_asynchronously() {
		when(processor.execute(any(Job_Task.class))).thenReturn(active());
		
		// Execute start task asynchronously
		transaction(() -> {
		    List<TaskId> successors = executeTask(jobId,start.getTaskId());
		    assertTrue(successors.isEmpty());
		    
		});
		
		// Complete start task
		transaction(() -> {
		    List<TaskId> successors = updateTask(jobId,start.getTaskId(), COMPLETED);
		    assertSuccessors(successors, split);
		    
		});

		// Execute split task asynchronously
		transaction(() -> {
		    List<TaskId> successors = executeTask(jobId,split.getTaskId());
		    assertTrue(successors.isEmpty());
		});
		
		// Complete split task
		transaction(() -> {
		    List<TaskId> successors = updateTask(jobId,split.getTaskId(), COMPLETED);
		    assertSuccessors(successors, branchA0,branchB0);
		});
		
		// Execute branchA0 task asynchronously
		transaction(() -> {
		    List<TaskId> successors = executeTask(jobId,branchA0.getTaskId());
		    assertTrue(successors.isEmpty());
		});
		
		// Complete branchA0 task
		transaction(() -> {
		    List<TaskId>successors = updateTask(jobId,branchA0.getTaskId(), COMPLETED);
		    assertSuccessors(successors, branchA1);
		});

		// Execute branchA1 task asynchronously
		transaction(() -> {
		    List<TaskId> successors = executeTask(jobId,branchA1.getTaskId());
		    assertTrue(successors.isEmpty());
		});
		
		// Complete branchA1 task
		transaction(() -> {
		    List<TaskId> successors = updateTask(jobId,branchA1.getTaskId(), COMPLETED);
		    assertTrue(successors.isEmpty());
		});

		// Execute branchB0 task asynchronously
		transaction(() -> {
		    List<TaskId> successors = executeTask(jobId,branchB0.getTaskId());
		    assertTrue(successors.isEmpty());
		});
		
		// Complete branchB0 task
		transaction(() -> {
		    List<TaskId> successors = updateTask(jobId,branchB0.getTaskId(), COMPLETED);
		    assertSuccessors(successors, join);
		});
		
		// Execute join task asynchronously
		transaction(() -> {
		    List<TaskId> successors = executeTask(jobId,join.getTaskId());
		    assertTrue(successors.isEmpty());
		});
		
		// Complete branchA0 task
		transaction(() -> {
		    List<TaskId> successors = updateTask(jobId,join.getTaskId(), COMPLETED);
		    assertSuccessors(successors, end);
		});
		
		// Execute end task asynchronously
		transaction(() -> {
		    List<TaskId> successors = executeTask(jobId,end.getTaskId());
		    assertTrue(successors.isEmpty());
		});
		
		// Complete end task
		transaction(() -> {
		    List<TaskId >successors = updateTask(jobId,end.getTaskId(), COMPLETED);
		    assertTrue(successors.isEmpty());
		});
		
	}
	
	@After
	public void verify_job_completed() {
		JobProgress progress = jobs.getJobProgress(jobId);
		assertEquals(0,progress.getActiveCount());
		assertEquals(0,progress.getFailedCount());
		assertEquals(0,progress.getReadyCount());
		assertEquals(7,progress.getCompletedCount());
	}
	

	
}
