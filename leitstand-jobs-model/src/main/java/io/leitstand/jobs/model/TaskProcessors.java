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

import static io.leitstand.commons.model.ObjectUtil.optional;
import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import io.leitstand.commons.model.ObjectUtil;
import io.leitstand.jobs.service.JobApplication;
import io.leitstand.jobs.service.JobType;
import io.leitstand.jobs.service.TaskType;

public class TaskProcessors {

    private static final Logger LOG = Logger.getLogger(TaskProcessors.class.getName());
    
	public static TaskProcessors application(JobApplication application) {
		return new TaskProcessors(application);
	}
	
	private JobApplication jobApplication;
	private JobType jobType;
	private TaskProcessor defaultProcessor;
	private Map<TaskType,TaskProcessor> processors;
	
	protected TaskProcessors() {
		// CDI
	}
	
	protected TaskProcessors(JobApplication jobApplication) {
		this.jobApplication = jobApplication;
		this.processors = new HashMap<>();
	}
	
	public TaskProcessors jobType(JobType jobType) {
		this.jobType = jobType;
		return this;
	}

	public TaskProcessors taskProcessor(TaskType taskType, Supplier<TaskProcessor> taskProcessor) {
	    return taskProcessor(taskType,
	                         taskProcessor.get());
	}
	
	public TaskProcessors taskProcessor(TaskType taskType, TaskProcessor taskProcessor) {
		processors.put(taskType,taskProcessor);
		LOG.fine(() -> format("Register %s task processor for %s task type in %s jobs in %s applications",
		                      taskProcessor.getClass().getName(),
		                      taskType,
		                      optional(jobType, JobType::getValue, "*"),
		                      jobApplication));
		return this;
	}

	public TaskProcessors defaultProcessor(TaskProcessor taskProcessor) {
		this.defaultProcessor = taskProcessor;
        LOG.fine(() -> format("Register %s task processor as default task processor for %s jobs in %s applications",
                              taskProcessor.getClass().getName(),
                              ObjectUtil.optional(jobType, JobType::getValue, "*"),
                              jobApplication));
		return this;
	}
	
	public boolean providesTaskProcessorsFor(JobApplication jobApplication, JobType jobType) {
		if(this.jobApplication.equals(jobApplication)) {
			return this.jobType == null || this.jobType.equals(jobType);
		}
		return false;
	}
	
	public TaskProcessor getTaskProcessor(TaskType taskType) {
		TaskProcessor taskProcessor = processors.get(taskType);
		if(taskProcessor != null) {
			return taskProcessor;
		}
		return defaultProcessor;
	}

}
