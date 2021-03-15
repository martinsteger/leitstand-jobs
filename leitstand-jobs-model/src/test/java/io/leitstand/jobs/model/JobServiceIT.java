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

import static io.leitstand.inventory.service.ElementId.randomElementId;
import static io.leitstand.inventory.service.ElementName.elementName;
import static io.leitstand.jobs.service.JobApplication.jobApplication;
import static io.leitstand.jobs.service.JobId.randomJobId;
import static io.leitstand.jobs.service.JobName.jobName;
import static io.leitstand.jobs.service.JobSchedule.newJobSchedule;
import static io.leitstand.jobs.service.JobSubmission.newJobSubmission;
import static io.leitstand.jobs.service.JobType.jobType;
import static io.leitstand.jobs.service.State.ACTIVE;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.State.READY;
import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static io.leitstand.jobs.service.TaskName.taskName;
import static io.leitstand.jobs.service.TaskSubmission.newTaskSubmission;
import static io.leitstand.jobs.service.TaskTransitionSubmission.newTaskTransitionSubmission;
import static io.leitstand.jobs.service.TaskType.taskType;
import static io.leitstand.security.auth.UserName.userName;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import io.leitstand.commons.db.DatabaseService;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Repository;
import io.leitstand.inventory.service.ElementName;
import io.leitstand.jobs.service.JobApplication;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobName;
import io.leitstand.jobs.service.JobProgress;
import io.leitstand.jobs.service.JobSettings;
import io.leitstand.jobs.service.JobSubmission;
import io.leitstand.jobs.service.JobType;
import io.leitstand.jobs.service.TaskName;
import io.leitstand.jobs.service.TaskSubmission;
import io.leitstand.jobs.service.TaskTransitionSubmission;
import io.leitstand.jobs.service.TaskType;
import io.leitstand.security.auth.UserContext;

public class JobServiceIT extends JobsIT{
    
    private static final JobId JOB_ID = randomJobId();
    private static final JobName JOB_NAME   = jobName("job");
    private static final JobType JOB_TYPE   = jobType("type");
    private static final JobApplication JOB_APP = jobApplication("app");
    private static final Date JOB_SCHEDULE = new Date(System.currentTimeMillis()/1000);
    
    
	private static final TaskName START 	= taskName("start");
	private static final TaskName SPLIT 	= taskName("split");
	private static final TaskName BRANCH_A0 = taskName("branch_a_0");
	private static final TaskName BRANCH_A1 = taskName("branch_a_1");
	private static final TaskName BRANCH_B0 = taskName("branch_b_0");
	private static final TaskName JOIN		= taskName("join");
	private static final TaskName END		= taskName("end");
	
	private static final TaskType UNIT		= taskType("unit");
	
	private static final TaskSubmission task(TaskName taskName) {
		return newTaskSubmission()
			   .withTaskId(randomTaskId())
			   .withTaskName(taskName)
			   .withTaskType(UNIT)
			   .build();
	}
	
    private static final TaskSubmission task(TaskName taskName,
                                             ElementName elementName) {
        return newTaskSubmission()
               .withTaskId(randomTaskId())
               .withTaskName(taskName)
               .withTaskType(UNIT)
               .withElementId(randomElementId())
               .withElementName(elementName)
               .build();
    }
    
    private static final TaskSubmission canaryTask(TaskName taskName,
                                                   ElementName elementName) {
         return newTaskSubmission()
                .withTaskId(randomTaskId())
                .withTaskName(taskName)
                .withTaskType(UNIT)
                .withElementId(randomElementId())
                .withElementName(elementName)
                .withCanary(true)
                .build();
    }
	
	private static final TaskTransitionSubmission transition(TaskSubmission from,
															 TaskSubmission to){
		return newTaskTransitionSubmission()
			   .from(from.getTaskId())
			   .to(to.getTaskId())
			   .build();
	}
	
	private DefaultJobService jobs;
	private DefaultJobTaskService tasks;
	private TaskProcessor processor;
	private TaskSubmission start; 	
	private TaskSubmission split; 	
	private TaskSubmission branchA0; 
	private TaskSubmission branchA1; 
	private TaskSubmission branchB0; 
	private TaskSubmission join;		
	private TaskSubmission end; 
	private Repository repository;
	private InventoryClient inventory;
	
	@Before
	public void create_job() {
		// Create repository to test DB interaction
		repository = new Repository(getEntityManager());

		UserContext userContext = mock(UserContext.class);
		when(userContext.getUserName()).thenReturn(userName("dummy"));
		
		this.inventory = mock(InventoryClient.class);
		
		// Create job service and IT job definition.
		jobs = new DefaultJobService(repository, 
		                             new JobProvider(repository),
									 mock(DatabaseService.class),
									 inventory,    
									 new JobEditor(repository),
									 mock(Messages.class),
									 userContext);

		start 	 = canaryTask(START,elementName("start"));
	 	split 	 = task(SPLIT);
	 	branchA0 = task(BRANCH_A0,elementName("a0"));
	 	branchA1 = canaryTask(BRANCH_A1,elementName("a1"));
	 	branchB0 = canaryTask(BRANCH_B0,elementName("b0"));
	 	join	 = task(JOIN);
	 	end 	 = task(END,elementName("end"));
		
		JobSubmission job = newJobSubmission()
						    .withJobApplication(JOB_APP)
						    .withJobName(JOB_NAME)
						    .withJobType(JOB_TYPE)
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
							.withSchedule(newJobSchedule()
							              .withStartTime(JOB_SCHEDULE))
							.build();
		
		transaction(() -> {
		    // Store job
		    jobs.storeJob(JOB_ID, 
					      job);
		    
		    // Make job eligible for deployment
		    jobs.commitJob(JOB_ID);
		});
		processor = mock(TaskProcessor.class);
		TaskProcessorDiscoveryService discovery = mock(TaskProcessorDiscoveryService.class);
		when(discovery.findElementTaskProcessor(any(Job_Task.class))).thenReturn(processor);
		
		this.tasks = new DefaultJobTaskService(repository, 
		                                       new JobProvider(repository),
		                                       new TaskProcessingService(discovery));
		
	}
	
	@Test
	public void get_job_settings() {
	    
	    transaction(() -> {
	        JobSettings settings =  jobs.getJobSettings(JOB_ID);
	        assertEquals(JOB_ID, settings.getJobId());
	        assertEquals(JOB_NAME,settings.getJobName());
	        assertEquals(JOB_TYPE,settings.getJobType());
	        assertEquals(JOB_APP,settings.getJobApplication());
	        assertEquals(READY,settings.getJobState());
	    });
	    
	}
	
    @Test
    public void get_job_progress() {
       transaction(() -> {
            tasks.updateTask(JOB_ID, start.getTaskId(), COMPLETED);
            tasks.updateTask(JOB_ID, split.getTaskId(), COMPLETED);
            tasks.updateTask(JOB_ID, branchA0.getTaskId(), COMPLETED);
            tasks.updateTask(JOB_ID, branchA1.getTaskId(), READY);
            tasks.updateTask(JOB_ID, branchB0.getTaskId(), ACTIVE);
       });
       
       transaction(() -> {
           JobProgress jobProgress = jobs.getJobProgress(JOB_ID);
           assertThat(jobProgress.getCompletedCount(),is(3));
           assertThat(jobProgress.getActiveCount(),is(1));
           assertThat(jobProgress.getReadyCount(),is(1));
           assertThat(jobProgress.getWaitingCount(),is(2));
           
       });
    }
	
}
