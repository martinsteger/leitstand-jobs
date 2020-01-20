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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.leitstand.jobs.service.JobApplication;
import io.leitstand.jobs.service.JobType;
import io.leitstand.jobs.service.TaskType;

public class TaskProcessors {

	public static TaskProcessors application(JobApplication application) {
		return new TaskProcessors(application);
	}
	
	private JobApplication jobApplication;
	private JobType jobType;
	private TaskProcessor defaultTaskProcessor;
	private Map<TaskType,TaskProcessor> taskProcessors;
	
	protected TaskProcessors() {
		// CDI
	}
	
	protected TaskProcessors(JobApplication jobApplication) {
		this.jobApplication = jobApplication;
		this.taskProcessors = new HashMap<>();
	}
	
	public TaskProcessors jobType(JobType jobType) {
		this.jobType = jobType;
		return this;
	}

	public TaskProcessors taskProcessor(TaskType taskType, Supplier<TaskProcessor> taskProcessor) {
		taskProcessors.put(taskType,taskProcessor.get());
		return this;
	}
	
	public TaskProcessors taskProcessor(TaskType taskType, TaskProcessor taskProcessor) {
		taskProcessors.put(taskType,taskProcessor);
		return this;
	}

	public TaskProcessors defaultProcessor(TaskProcessor processor) {
		this.defaultTaskProcessor = processor;
		return this;
	}
	
	public boolean providesTaskProcessorsFor(JobApplication jobApplication, JobType jobType) {
		if(this.jobApplication.equals(jobApplication)) {
			return this.jobType == null || this.jobType.equals(jobType);
		}
		return false;
	}
	
	public TaskProcessor getTaskProcessor(TaskType taskType) {
		TaskProcessor taskProcessor = taskProcessors.get(taskType);
		if(taskProcessor != null) {
			return taskProcessor;
		}
		return defaultTaskProcessor;
	}

}
