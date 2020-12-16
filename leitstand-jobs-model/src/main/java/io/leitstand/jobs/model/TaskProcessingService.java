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

import static io.leitstand.commons.model.ObjectUtil.isDifferent;
import static io.leitstand.jobs.service.TaskState.COMPLETED;
import static io.leitstand.jobs.service.TaskState.CONFIRM;
import static java.lang.String.format;
import static java.util.logging.Logger.getLogger;

import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import io.leitstand.jobs.service.TaskState;

@Dependent
public class TaskProcessingService  {
	
    private static final Logger LOG = getLogger(TaskProcessingService.class.getName());
    
	private TaskProcessorDiscoveryService processors;
	private Event<TaskStateChangedEvent> sink;

	
	@Inject
	public TaskProcessingService(TaskProcessorDiscoveryService processors, 
	                             Event<TaskStateChangedEvent> sink) {
		this.processors = processors;
		this.sink = sink;
	}
	
	public void executeTask(Job_Task task){
		TaskState state = task.getTaskState();
		try {
    		// Load the task processor for the specified task...
    		TaskProcessor processor = processors.findElementTaskProcessor(task);
    
    		if(processor != null) {
                LOG.fine(() -> format("%s task processor for %s task (%s) of %s job (%s) in %s." , 
                                      processor.getClass().getName(),
                                      task.getTaskName(), 
                                      task.getTaskId(), 
                                      task.getJobName(), 
                                      task.getJobId(), 
                                      task.getJobApplication()));

    			TaskState newState = processor.execute(task);
    			task.setTaskState(newState);
    		} else {
    	        // An executable task with no processor is either
    	        // - a fork task, that has to be completed in order to fork the task flow into multiple branches or
    	        // - a join task, where all predecessors have been completed (otherwise the task would be blocked)
    	        //   that has to be completed in order to process the successor.
    	        // In summary: an executable task without processor is done as per definition.
                LOG.fine(() -> format("No task processor found for %s task (%s) of %s job (%s) in %s." , 
                                      task.getTaskName(), 
                                      task.getTaskId(), 
                                      task.getJobName(), 
                                      task.getJobId(), 
                                      task.getJobApplication()));
    		    task.setTaskState(COMPLETED);
    		}
    		
            if(task.isFailed()) {
                task.getJob().failed();
            } else if(task.isSucceeded()) {
                task.getJob().completed();
            }
		} finally {
		    if(isDifferent(state,task.getTaskState())){
	            sink.fire(new TaskStateChangedEvent(task));
		    }
		}
	}
	
	public void updateTask(Job_Task task, TaskState state) {
       try {
            if(task.isTerminated()) {
                // Ignore all updates on terminated tasks.
                return;
            }
            Job job = task.getJob();
            
            if(task.isCanary() && task.isActive() && state == COMPLETED) {
                task.setTaskState(CONFIRM);
                job.setJobState(CONFIRM);
                return;
            }
            
            if(state == COMPLETED && task.isSuspended()) {
                // Change job from CONFIRM to ACTIVE state
                // if no other tasks are in CONFIRM state
                task.setTaskState(state);
                job.confirmed();
            } else {
                task.setTaskState(state);
            }

            if(task.isFailed()) {
                job.failed();
            } else {
                job.completed();
            }
        } finally {
            sink.fire(new TaskStateChangedEvent(task));
        }
	}

}
