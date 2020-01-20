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

import static io.leitstand.jobs.service.TaskState.ACTIVE;
import static io.leitstand.jobs.service.TaskState.COMPLETED;
import static io.leitstand.jobs.service.TaskState.FAILED;
import static io.leitstand.jobs.service.TaskState.READY;
import static io.leitstand.jobs.service.TaskState.REJECTED;

public class JobTaskMother {

	public static Job_Task completedTask() {
		Job_Task task = new Job_Task();
		task.setTaskState(COMPLETED);
		return task;
	}

	public static Job_Task activeTask() {
		Job_Task task = new Job_Task();
		task.setTaskState(ACTIVE);
		return task;
	}

	public static Job_Task rejectedTask() {
		Job_Task task = new Job_Task();
		task.setTaskState(REJECTED);
		return task;
	}

	public static Job_Task failedTask() {
		Job_Task task = new Job_Task();
		task.setTaskState(FAILED);
		return task;
	}

	public static Job_Task readyTask() {
		Job_Task task = new Job_Task();
		task.setTaskState(READY);
		return task;
	}

	
	
	
}
