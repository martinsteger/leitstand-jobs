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

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Logger.getLogger;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class JobEventLoop extends BaseEventLoop {
	
	private static final Logger LOG = getLogger(JobEventLoop.class.getName());
	private static final long MAX_WAIT_SECONDS = 5;
	
	@Resource
	private ManagedExecutorService wm;
	
	@Inject
	private JobScheduler scheduler;
	
	private Pause pause;
	
	@Override
	public void onStartup() {
	    this.pause = new Pause(MAX_WAIT_SECONDS, 
	                           SECONDS);
	    super.onStartup();
	}
	
	@Override
	public void run() {
	    try {
    		LOG.info("Job event loop started.");
    		
    		while(isActive()) {
    		    scheduleJobs();
    		}
    		
    		LOG.info("Job event loop stopped.");
	    } catch (Exception e) {
	        LOG.severe("Job event loop crashed: "+e.getMessage());
	        stopEventLoop();
	        startEventLoop();
	    }
	}

    protected void scheduleJobs() throws InterruptedException {
        int failedJobs    = scheduler.markFailedJobs();
        int activatedJobs = scheduler.startScheduledJobs();
        int tasksReady    = scheduler.markTasksEligibleForExecution();
        int jobsToConfirm = scheduler.markJobsWaitingForConfirmation();
        int completedJobs = scheduler.markCompletedJobs();
        
        LOG.fine(() -> format("Job event loop: %d job(s) started, %d tasks eligible for execution, %d job(s) completed, %d job(s) failed, %d job(s) wait for confirmation,",
                              activatedJobs,
                              tasksReady,
                              completedJobs,
                              failedJobs,
                              jobsToConfirm));
        
        if(activatedJobs == 0 && tasksReady == 0) {
            pause.sleep();
        } else {
            pause.reset();
        }
    }

}

