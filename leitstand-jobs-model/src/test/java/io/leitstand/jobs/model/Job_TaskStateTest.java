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
import static io.leitstand.jobs.service.TaskState.CANCELLED;
import static io.leitstand.jobs.service.TaskState.COMPLETED;
import static io.leitstand.jobs.service.TaskState.CONFIRM;
import static io.leitstand.jobs.service.TaskState.FAILED;
import static io.leitstand.jobs.service.TaskState.READY;
import static io.leitstand.jobs.service.TaskState.REJECTED;
import static io.leitstand.jobs.service.TaskState.SKIPPED;
import static io.leitstand.jobs.service.TaskState.TIMEOUT;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.leitstand.jobs.service.TaskState;

@RunWith(Parameterized.class)
public class Job_TaskStateTest {
	
	
	private static Object[] assertion(TaskState state,
									  String message,
									  Function<Job_Task,Boolean> assertion)  {
		return new Object[] {state,message,assertion};
		
	}
 	@Parameters
	public static Collection<Object[]> mappings(){
		return asList(new Object[][] {
			assertion(ACTIVE, "is active",task -> task.isActive()),
			assertion(ACTIVE, "is not ready", task-> !task.isReady()),
			assertion(ACTIVE, "is not resumable", task -> !task.isReady()),
			assertion(ACTIVE, "is not rejected", task -> !task.isRejected()),
			assertion(SKIPPED, "is skipped", task -> task.isSkipped()),
			assertion(SKIPPED,"is not ready", task -> !task.isReady()),
			assertion(SKIPPED,"is not active", task -> !task.isActive()),
			assertion(SKIPPED,"is resumable",task -> task.isResumable()),	
			assertion(CANCELLED,"is cancelled", task -> task.isCancelled()),
			assertion(CANCELLED,"is terminated", task -> task.isTerminated()),
			assertion(CANCELLED,"is not ready", task -> !task.isReady()),
			assertion(CANCELLED,"is not active", task -> !task.isActive()),
			assertion(CANCELLED,"is resumable", task -> task.isResumable()),
			assertion(FAILED, "is failed", task -> task.isFailed()),
			assertion(FAILED, "is terminated", task -> task.isTerminated()),
			assertion(FAILED, "is not active", task -> !task.isActive()),
			assertion(FAILED, "is not resumable", task -> task.isResumable()),
			assertion(CONFIRM,"is suspended", task -> task.isSuspended()),
			assertion(CONFIRM, "is not ready", task -> !task.isReady()),
			assertion(CONFIRM,"is not active", task -> !task.isActive()),
			assertion(COMPLETED,"is succeeded",task -> task.isSucceeded()),
			assertion(COMPLETED,"is not ready",task -> !task.isReady()),
			assertion(COMPLETED,"is not active",task -> !task.isActive()),
			assertion(READY,"is  ready", task -> task.isReady()),
			assertion(READY,"is not terminated", task -> !task.isTerminated()),
			assertion(READY,"is not active", task -> !task.isActive()),
			assertion(REJECTED,"is terminated",task -> task.isTerminated()),
			assertion(REJECTED,"is rejected", task -> task.isRejected()),
			assertion(REJECTED,"is not ready", task -> !task.isReady()), 
			assertion(REJECTED,"is not active", task -> !task.isActive()),
			assertion(TIMEOUT,"is timed out",task -> task.isTimedOut()),
			assertion(TIMEOUT,"is not ready",task -> !task.isReady()),
			assertion(TIMEOUT,"is not active",task -> !task.isActive()),
			assertion(TIMEOUT,"is not terminated",task -> !task.isTerminated()),
			assertion(TIMEOUT,"is not succeeded",task -> !task.isSucceeded()),
			assertion(TIMEOUT,"is not failed",task -> !task.isFailed()),

		});
	}
	
	
	
	private Function<Job_Task,Boolean> assertion;
	private TaskState taskState;
	private String description;
	private Job_Task task;
	public Job_TaskStateTest(TaskState state, String description, Function<Job_Task,Boolean> assertion ) {
		this.taskState = state;
		this.assertion = assertion;
		this.description = description;
		this.task = new Job_Task();
	}
	
	@Test
	public void stateMapping() {
		task.setTaskState(taskState);
		assertTrue(taskState+" task "+description,assertion.apply(task));
	}

	
}
