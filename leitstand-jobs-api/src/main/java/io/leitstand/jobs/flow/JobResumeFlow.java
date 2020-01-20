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
package io.leitstand.jobs.flow;

import javax.inject.Inject;

import io.leitstand.commons.flow.ControlFlow;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobService;
import io.leitstand.jobs.service.JobTaskService;
import io.leitstand.jobs.service.TaskId;


@ControlFlow
public class JobResumeFlow {

	private JobService jobService;
	private JobTaskService taskService;
	
	@Inject
	public JobResumeFlow(JobService jobService, JobTaskService taskService) {
		this.jobService = jobService;
		this.taskService = taskService;
	}
	
	public void resumeJob(JobId jobId) {
		for(TaskId task : jobService.resumeJob(jobId)) {
			executeTask(jobId, task, 2);
		}
		
	}

	private void executeTask(JobId jobId, TaskId successor, int retryPermits) {
		try {
			taskService.executeTask(jobId,successor);
		} catch (Exception e) {
			if(--retryPermits == 0) {
				throw e;
			}
			executeTask(jobId,successor, retryPermits);
		}
	}


	
}
