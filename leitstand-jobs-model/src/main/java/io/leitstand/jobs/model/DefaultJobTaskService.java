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
import static io.leitstand.commons.model.ObjectUtil.not;
import static io.leitstand.commons.model.ObjectUtil.optional;
import static io.leitstand.jobs.model.Job.findByJobId;
import static io.leitstand.jobs.model.Job_Task.findByTaskId;
import static io.leitstand.jobs.service.JobTaskInfo.newJobTaskInfo;
import static io.leitstand.jobs.service.ReasonCode.JOB0203E_TASK_OWNED_BY_OTHER_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0204E_CANNOT_MODIFY_TASK_OF_RUNNING_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0205E_CANNOT_MODIFY_COMPLETED_TASK;
import static io.leitstand.jobs.service.ReasonCode.JOB0206I_TASK_PARAMETER_UPDATED;
import static io.leitstand.jobs.service.TaskState.ACTIVE;
import static io.leitstand.jobs.service.TaskState.COMPLETED;
import static io.leitstand.jobs.service.TaskState.CONFIRM;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

import java.util.List;

import javax.enterprise.event.Event;
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
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskState;

@Service
public class DefaultJobTaskService implements JobTaskService{

	@Inject
	@Jobs
	private Repository repository;
	
	@Inject
	private InventoryClient inventory;
	
	@Inject
	private Event<TaskStateChangedEvent> sink;
	
	@Inject
	private TaskProcessingService processor;
	
	@Inject
	private Messages messages;
	
	public DefaultJobTaskService() {
		// EJB ctor
	}
	
	DefaultJobTaskService(Repository repository,
						  TaskProcessingService processor,
						  Event<TaskStateChangedEvent> sink){
		this.repository = repository;
		this.processor  = processor;
		this.sink		= sink;
	}
	
	@Override
	public List<TaskId> updateTask(JobId jobId, TaskId taskId, TaskState state) {
		// Serialize updates on a task to search for successors.
		Job job = repository.execute(findByJobId(jobId,PESSIMISTIC_WRITE));
		Job_Task task = job.getTask(taskId);
		try {
			if(task.isTerminated()) {
				// Ignore all updates on terminated tasks.
				return emptyList();
			}
			if(task.isCanary() && task.isActive() && state == COMPLETED) {
				task.setTaskState(CONFIRM);
				job.setJobState(CONFIRM);
				return emptyList();
			}
			
			if(state == COMPLETED) {
				if(task.isSuspended()) {
					task.setCanary(false);
					job.setJobState(ACTIVE);
				}
				task.setTaskState(COMPLETED);
				List<TaskId> successors = task.getSuccessors()
											  .stream()
											  .map(Job_Task_Transition::getTo)
											  .filter(not(Job_Task::isBlocked))
											  .map(Job_Task::getTaskId)
											  .collect(toList());
				if(successors.isEmpty()) {
					job.completed();
				}
				return successors;
			}
			task.setTaskState(state);
			if(task.isFailed()) {
				job.failed();
			}
			return emptyList();
		} finally {
			sink.fire(new TaskStateChangedEvent(task));
		}
	}

	@Override
	public List<TaskId> executeTask(JobId jobId, TaskId taskId) {
		Job job = repository.execute(findByJobId(jobId,PESSIMISTIC_WRITE));
		Job_Task task = job.getTask(taskId);
		if(task.isReady()) {
			task.setTaskState(ACTIVE);
			List<TaskId>  successors = processor.execute(task)
											    .stream()
											    .filter(not(Job_Task::isBlocked))
											    .map(Job_Task::getTaskId)
											    .collect(toList());
			if(successors.isEmpty() && task.isTerminated()) {
				// Asynchronous task completed successfully.
				// Let job check for remaining tasks, otherwise mark job as completed.
				if(task.isSucceeded()) {
					job.completed();
					return emptyList();
				} 
				job.failed();
				return emptyList();
			}
			return successors;
		}
		return emptyList();
	}

	@Override
	public JobTaskInfo getJobTask(JobId jobId, TaskId taskId) {
		Job_Task task = repository.execute(findByTaskId(taskId));
		if(task == null) {
		    throw new EntityNotFoundException(ReasonCode.JOB0200E_TASK_NOT_FOUND, taskId);
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
        Job_Task task = repository.execute(findByTaskId(taskId));
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
