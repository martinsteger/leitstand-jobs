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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;


@ApplicationScoped
public class TaskProcessorDiscoveryService {

	@Inject
	private Instance<TaskProcessors> processors;
	
	public TaskProcessor findElementTaskProcessor(Job_Task task) {
		for(TaskProcessors tasks : processors) {
			if(tasks.providesTaskProcessorsFor(task.getJobApplication(), task.getJobType())) {
				return tasks.getTaskProcessor(task.getTaskType());
			}
		}
		return null;
	}
	
}
