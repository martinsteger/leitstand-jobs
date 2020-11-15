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

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.logging.Level.FINER;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.leitstand.commons.ShutdownListener;
import io.leitstand.commons.StartupListener;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobTaskService;
import io.leitstand.jobs.service.TaskId;

@ApplicationScoped
public class JobEventLoop implements Runnable, StartupListener, ShutdownListener{
	
	private static final Logger LOG = Logger.getLogger(JobEventLoop.class.getName());
	
	private static final int JOB_LIMIT = 20; // Don't process more than 20 jobs in parallel

	private volatile boolean active;
	
	@Resource
	private ManagedExecutorService wm;
	
	@Inject
	private TaskExpiryManager expiryManager;
	
	@Inject
	private JobScheduler scheduler;
	
	@Inject
	private JobTaskService executor;
	
	@Override
	public void onStartup() {
		startEventLoop();
	}

	@Override
	public void onShutdown() {
		stopEventLoop();
	}
	
	
	public void stopEventLoop() {
		this.active = false;
	}
	
	public void startEventLoop() {
		if(!active) {
			active = true;
			try {
				wm.execute(this);
			} catch (Exception e) {
				LOG.severe("Unable to start job event loop: "+e);
				LOG.log(FINER,e.getMessage(),e);
			}
			//TODO Maintain expiry date per task to support specific expiry periods.
			Date expired = new Date(currentTimeMillis()-MINUTES.toMillis(15));
			expiryManager.taskTimedout(expired);
		}
	}
	
	@Override
	public void run() {
		LOG.info("Job event loop started.");
		
		long waittime = 1;
		while(active) {
			int jobCount = scheduleJobsEligibleForExecution();
			int taskCount = runTasksEligibleForExecution();
			
		    if(jobCount == 0 && taskCount == 0) {
		        waittime = pause(waittime);
		    } else {
		        waittime = 1;
		    }
		}
		
		LOG.info("Job event loop stopped.");
	}

    private int runTasksEligibleForExecution() {
        int taskCount = 0;
        for(JobId job : scheduler.findRunningJobs(JOB_LIMIT)) {
            List<TaskId> tasks = scheduler.activateExecutableTasks(job);
            tasks.forEach(task -> executor.executeTask(job, task));
            taskCount+=tasks.size();
        }
        return taskCount;
    }

    private int scheduleJobsEligibleForExecution() {
        List<JobId> jobs = scheduler.findExecutableJobs(JOB_LIMIT);
        jobs.forEach(job -> scheduler.schedule(job));
        return jobs.size();
    }
	
	private long pause(long waittime){
	    try {
			LOG.fine(() -> format("No jobs or tasks eligible for execution. Sleep for %d seconds before polling for new tasks",waittime));
			sleep(TimeUnit.SECONDS.toMillis(waittime));
			// Wait time shall never exceed a minute (if no messages are there at all).
			return min(2*waittime, 60);
		} catch (InterruptedException e) {
			LOG.fine(() -> "Wait for domain events has been interrupted. Reset wait interval and proceed polling!");
			// Restore interrupt status.
			currentThread().interrupt();
			return 1; // Reset waittime
		}
	}

}

