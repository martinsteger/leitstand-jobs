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

import io.leitstand.jobs.service.TaskState;

/**
 * A <code>TaskProcessor</code> processes a single task of a {@link Job}.
 * <p>
 * Every application that creates jobs can provide task processors to process the tasks of the job.
 * By that, the job scheduler is fully extensible with respect to how to handle task.
 * An application must implement {@link ElementTaskProcessors} to expose all existing task processors.
 * </p>
 */
public interface TaskProcessor {
	
	TaskState execute(Job_Task task);
	
}
