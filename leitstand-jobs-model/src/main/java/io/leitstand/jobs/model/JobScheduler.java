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

import static io.leitstand.jobs.model.Job.findByJobId;
import static io.leitstand.jobs.model.Job.findRunnableJobs;
import static io.leitstand.jobs.service.TaskState.ACTIVE;
import static io.leitstand.jobs.service.TaskState.FAILED;
import static java.lang.String.format;
import static java.util.logging.Level.FINER;
import static java.util.stream.Collectors.toList;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import io.leitstand.commons.model.Repository;
import io.leitstand.commons.model.Service;
import io.leitstand.commons.tx.SubtransactionService;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobTaskService;
import io.leitstand.jobs.service.TaskId;

@Service
public class JobScheduler {
	
	private static final Logger LOG = Logger.getLogger(JobScheduler.class.getName());
	
	@Inject
	@Jobs
	private Repository repository;

	@Inject
	private JobTaskService service;
	
	@Inject
	private Event<JobStateChangedEvent> jobStateEventSink;
	
	@Inject
	@Jobs
	private SubtransactionService tx;
	
	public List<JobId> findJobs(){
		Date now = new Date();
		return repository.execute(findRunnableJobs(now))
						 .stream()
						 .map(Job::getJobId)
						 .collect(toList());
	}
	
	public void schedule(JobId jobId){
		try{
		    Job job = repository.execute(findByJobId(jobId));
			job.setJobState(ACTIVE);
			jobStateEventSink.fire(new JobStateChangedEvent(job));
			List<TaskId> tasks = service.executeTask(job.getJobId(),
													 job.getStart().getTaskId());
			while(!tasks.isEmpty()) {
				List<TaskId> successors = new LinkedList<>();
				for(TaskId taskId : tasks) {
					successors.addAll(service.executeTask(job.getJobId(),
														  taskId));
				}
				tasks = successors;
			}
			job.completed();
		} catch (Exception e){
		    // Transaction might be marked for rollback.
		    // Start new transaction to mark job as failed.
		    tx.run((r) -> {
		        Job job = r.execute(findByJobId(jobId));
	            job.setJobState(FAILED);
	            jobStateEventSink.fire(new JobStateChangedEvent(job));	        
	            LOG.info(()->format("Cannot start job %s (%s): %s",
	                    job.getJobName(),
	                    job.getJobId(),
	                    e.getMessage()));
		    });
		    
			LOG.log(FINER,e.getMessage(),e);
		}
	}
	

}
