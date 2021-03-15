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

import static io.leitstand.commons.messages.MessageFactory.createMessage;
import static io.leitstand.commons.model.ObjectUtil.isDifferent;
import static io.leitstand.commons.model.ObjectUtil.optional;
import static io.leitstand.jobs.model.Job_Task.findTaskById;
import static io.leitstand.jobs.service.JobTaskInfo.newJobTaskInfo;
import static io.leitstand.jobs.service.ReasonCode.JOB0200E_TASK_NOT_FOUND;
import static io.leitstand.jobs.service.ReasonCode.JOB0203E_TASK_OWNED_BY_OTHER_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0204E_CANNOT_MODIFY_TASK_OF_RUNNING_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0205E_CANNOT_MODIFY_COMPLETED_TASK;
import static io.leitstand.jobs.service.ReasonCode.JOB0206I_TASK_PARAMETER_UPDATED;

import javax.inject.Inject;
import javax.json.JsonObject;

import io.leitstand.commons.ConflictException;
import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Repository;
import io.leitstand.commons.model.Service;
import io.leitstand.inventory.service.ElementSettings;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobTaskInfo;
import io.leitstand.jobs.service.JobTaskService;
import io.leitstand.jobs.service.ReasonCode;
import io.leitstand.jobs.service.State;
import io.leitstand.jobs.service.TaskId;

@Service
public class DefaultJobTaskService implements JobTaskService{

	@Inject
	@Jobs
	private Repository repository;
	
	@Inject
	private JobProvider jobs;
	
	@Inject
	private InventoryClient inventory;
	
	@Inject
	private TaskProcessingService processor;
	
	@Inject
	private Messages messages;
	
	public DefaultJobTaskService() {
		// CDI 
	}
	
	DefaultJobTaskService(Repository repository,
	                      JobProvider jobs,
						  TaskProcessingService processor){
		this.repository = repository;
		this.jobs = jobs;
		this.processor  = processor;
	}
	
	@Override
	public void updateTask(JobId jobId, TaskId taskId, State state) {
		Job job = jobs.fetchJob(jobId);
		Job_Task task = job.getTask(taskId);
		if(task == null) {
		    throw new EntityNotFoundException(JOB0200E_TASK_NOT_FOUND,
		                                      taskId);
		}
		processor.updateTask(task,state);
	}

	@Override
	public void executeTask(JobId jobId, TaskId taskId) {
        Job job = jobs.fetchJob(jobId);
		Job_Task task = job.getTask(taskId);
        if(task == null) {
            throw new EntityNotFoundException(JOB0200E_TASK_NOT_FOUND,
                                              taskId);
        }
		processor.executeTask(task);
	}

	@Override
	public JobTaskInfo getJobTask(JobId jobId, TaskId taskId) {
		Job_Task task = repository.execute(findTaskById(taskId));
		if(task == null) {
		    throw new EntityNotFoundException(JOB0200E_TASK_NOT_FOUND, taskId);
		}
		
		Job job = task.getJob();
		if(isDifferent(jobId, task.getJobId())){
		    throw new ConflictException(JOB0203E_TASK_OWNED_BY_OTHER_JOB, taskId);
		}
		
		ElementSettings element = inventory.getElementSettings(task);
		
		return newJobTaskInfo()
			   .withGroupId(optional(element, ElementSettings::getGroupId))
			   .withGroupName(optional(element, ElementSettings::getGroupName))
			   .withGroupType(optional(element,ElementSettings::getGroupType))
			   .withJobId(job.getJobId())
			   .withJobName(job.getJobName())
			   .withJobType(job.getJobType())
			   .withJobApplication(job.getJobApplication())
			   .withElementId(optional(element,ElementSettings::getElementId))
			   .withElementName(optional(element,ElementSettings::getElementName))
			   .withElementAlias(optional(element,ElementSettings::getElementAlias))
			   .withElementRole(optional(element,ElementSettings::getElementRole))
			   .withTaskId(task.getTaskId())
			   .withTaskName(task.getTaskName())
			   .withTaskType(task.getTaskType())
			   .withTaskState(task.getTaskState())
			   .withDateLastModified(task.getDateModified())
			   .withParameter(task.getParameters())
			   .build();	
	}

    @Override
    public void setTaskParameter(JobId jobId, TaskId taskId, JsonObject parameters) {
        Job_Task task = repository.execute(findTaskById(taskId));
        if(task == null) {
            throw new EntityNotFoundException(ReasonCode.JOB0200E_TASK_NOT_FOUND, taskId);
        }
        
        Job job = task.getJob();
        if(isDifferent(jobId, task.getJobId())){
            throw new ConflictException(JOB0203E_TASK_OWNED_BY_OTHER_JOB, taskId);
        }
        
        if(job.isRunning()) {
            throw new ConflictException(JOB0204E_CANNOT_MODIFY_TASK_OF_RUNNING_JOB,taskId,jobId);
        }
        
        if(task.isSucceeded()) {
            throw new ConflictException(JOB0205E_CANNOT_MODIFY_COMPLETED_TASK,taskId);
        }
        task.setParameter(parameters);
        messages.add(createMessage(JOB0206I_TASK_PARAMETER_UPDATED, 
                                   job.getJobId(),
                                   job.getJobName(),
                                   task.getTaskId(),
                                   task.getTaskName()));
    }

}
