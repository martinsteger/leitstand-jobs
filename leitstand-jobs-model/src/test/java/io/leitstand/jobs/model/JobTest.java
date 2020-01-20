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
import static io.leitstand.jobs.service.TaskState.COMPLETED;
import static io.leitstand.security.auth.UserName.userName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

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
	
	
	
}
