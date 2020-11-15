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

import static io.leitstand.jobs.model.Job.findJobById;
import static io.leitstand.jobs.model.Job.findRunnableJobs;
import static io.leitstand.jobs.model.Job_Task.findSuccessorsOfCompletedTasks;
import static io.leitstand.jobs.service.TaskState.ACTIVE;
import static io.leitstand.jobs.service.TaskState.FAILED;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.logging.Level.FINER;
import static java.util.stream.Collectors.toList;
import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

import java.util.Date;
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
	
	/**
	 * Returns all jobs that are eligible for execution.
	 * @return List of IDs of all executable tasks.
	 */
	public List<JobId> findExecutableJobs(int limit){
		Date now = new Date();
		return repository.execute(findRunnableJobs(now, limit))
						 .stream()
						 .map(Job::getJobId)
						 .collect(toList());
	}
	
	public List<JobId> findRunningJobs(int limit){
	    return repository.execute(Job.findRunningJobs(limit))
	                     .stream()
	                     .map(Job::getJobId)
	                     .collect(toList());
	}
	
	public List<TaskId> activateExecutableTasks(JobId jobId){
	    // Make sure to have exclusive access to the job when changing tasks
	    // from READY to ACTIVE state to avoid duplicate execution of the
	    // same task.
	    Job job = repository.execute(findJobById(jobId, PESSIMISTIC_WRITE));
	    if(job.getStart().isSucceeded()) {
    	    return repository.execute(findSuccessorsOfCompletedTasks(job, PESSIMISTIC_WRITE))
    	                     .stream()
    	                     .filter(Job_Task::isEligibleForExecution)
    	                     .map(Job_Task::getTaskId)
    	                     .collect(toList());
	    }
	    Job_Task start = job.getStart();
	    start.setTaskState(ACTIVE);
	    return asList(start.getTaskId());
	}
	
	public void schedule(JobId jobId){
		try{
		    Job job = repository.execute(findJobById(jobId));
			job.setJobState(ACTIVE);
			jobStateEventSink.fire(new JobStateChangedEvent(job));
			service.executeTask(job.getJobId(),
								job.getStart().getTaskId());
		} catch (Exception e){
		    // Transaction might be marked for rollback.
		    // Start new transaction to mark job as failed.
		    tx.run((r) -> {
		        Job job = r.execute(findJobById(jobId));
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
