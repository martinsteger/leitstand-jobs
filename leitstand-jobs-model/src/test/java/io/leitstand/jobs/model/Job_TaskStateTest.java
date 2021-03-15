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

import static io.leitstand.jobs.service.State.ACTIVE;
import static io.leitstand.jobs.service.State.CANCELLED;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.State.CONFIRM;
import static io.leitstand.jobs.service.State.FAILED;
import static io.leitstand.jobs.service.State.NEW;
import static io.leitstand.jobs.service.State.READY;
import static io.leitstand.jobs.service.State.REJECTED;
import static io.leitstand.jobs.service.State.SKIPPED;
import static io.leitstand.jobs.service.State.TIMEOUT;
import static io.leitstand.jobs.service.State.WAITING;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.leitstand.jobs.service.State;

@RunWith(Parameterized.class)
public class Job_TaskStateTest {
	
	
	private static Object[] assertion(State state,
									  String message,
									  Function<Job_Task,Boolean> assertion)  {
		return new Object[] {state,message,assertion};
		
	}
 	@Parameters
	public static Collection<Object[]> mappings(){
		return asList(new Object[][] {
            assertion(NEW, "is not active",task -> !task.isActive()),
            assertion(NEW, "is not ready", task-> !task.isReady()),
            assertion(NEW, "is not resumable", task -> !task.isResumable()),
            assertion(NEW, "is not rejected", task -> !task.isRejected()),
            assertion(NEW, "is not waiting for execution", task -> !task.isWaiting()),
            assertion(NEW, "is not timed out", task -> !task.isTimedOut()),
            assertion(NEW, "is not eligible for execution", task -> !task.isEligibleForExecution()),
		    assertion(ACTIVE, "is active",task -> task.isActive()),
			assertion(ACTIVE, "is not ready", task-> !task.isReady()),
			assertion(ACTIVE, "is not resumable", task -> !task.isResumable()),
			assertion(ACTIVE, "is not rejected", task -> !task.isRejected()),
			assertion(ACTIVE, "is not waiting for execution", task -> !task.isWaiting()),
			assertion(ACTIVE, "is not timed out", task -> !task.isTimedOut()),
			assertion(ACTIVE, "is not eligible for execution", task -> !task.isEligibleForExecution()),
            assertion(SKIPPED, "is skipped", task -> task.isSkipped()),
			assertion(SKIPPED, "is not ready", task -> !task.isReady()),
			assertion(SKIPPED, "is not active", task -> !task.isActive()),
			assertion(SKIPPED, "is resumable",task -> task.isResumable()),
	        assertion(SKIPPED, "is not waiting for execution", task -> !task.isWaiting()),
            assertion(SKIPPED, "is not timed out", task -> !task.isTimedOut()),
            assertion(SKIPPED, "is not eligible for execution", task -> !task.isEligibleForExecution()),
	        assertion(CANCELLED, "is cancelled", task -> task.isCancelled()),
			assertion(CANCELLED, "is terminated", task -> task.isTerminated()),
			assertion(CANCELLED, "is not ready", task -> !task.isReady()),
			assertion(CANCELLED, "is not active", task -> !task.isActive()),
			assertion(CANCELLED, "is resumable", task -> task.isResumable()),
	        assertion(CANCELLED, "is not waiting for execution", task -> !task.isWaiting()),
	        assertion(CANCELLED, "is not timed out", task -> !task.isTimedOut()),
            assertion(CANCELLED, "is not eligible for execution", task -> !task.isEligibleForExecution()),
	        assertion(FAILED, "is failed", task -> task.isFailed()),
			assertion(FAILED, "is terminated", task -> task.isTerminated()),
			assertion(FAILED, "is not active", task -> !task.isActive()),
			assertion(FAILED, "is not resumable", task -> task.isResumable()),
	        assertion(FAILED, "is not waiting for execution", task -> !task.isWaiting()),
	        assertion(FAILED, "is not timed out", task -> !task.isTimedOut()),
	        assertion(FAILED, "is not eligible for execution", task -> !task.isEligibleForExecution()),
	        assertion(CONFIRM,"is suspended", task -> task.isSuspended()),
			assertion(CONFIRM, "is not ready", task -> !task.isReady()),
			assertion(CONFIRM, "is not active", task -> !task.isActive()),
	        assertion(CONFIRM, "is not waiting for execution", task -> !task.isWaiting()),
	        assertion(CONFIRM, "is not timed out", task -> !task.isTimedOut()),
	        assertion(CONFIRM, "is not eligible for execution", task -> !task.isEligibleForExecution()),
	        assertion(COMPLETED,"is succeeded",task -> task.isSucceeded()),
			assertion(COMPLETED,"is not ready",task -> !task.isReady()),
			assertion(COMPLETED,"is not active",task -> !task.isActive()),
	        assertion(COMPLETED, "is not waiting for execution", task -> !task.isWaiting()),
	        assertion(COMPLETED, "is not resumable", task -> !task.isResumable()),
            assertion(COMPLETED, "is not timed out", task -> !task.isTimedOut()),
            assertion(COMPLETED, "is not eligible for execution", task -> !task.isEligibleForExecution()),
			assertion(READY, "is ready", task -> task.isReady()),
			assertion(READY, "is not terminated", task -> !task.isTerminated()),
			assertion(READY, "is not active", task -> !task.isActive()),
	        assertion(READY, "is not waiting for execution", task -> !task.isWaiting()),
	        assertion(READY, "is resumable ", task -> task.isResumable()),
	        assertion(READY, "is not timed out", task -> !task.isTimedOut()),
            assertion(READY, "is eligible for execution", task -> task.isEligibleForExecution()),
	        assertion(REJECTED, "is terminated",task -> task.isTerminated()),
			assertion(REJECTED, "is rejected", task -> task.isRejected()),
			assertion(REJECTED, "is not ready", task -> !task.isReady()), 
			assertion(REJECTED, "is not active", task -> !task.isActive()),
	        assertion(REJECTED, "is not waiting for execution", task -> !task.isWaiting()),
	        assertion(REJECTED, "is resumable", task -> task.isResumable()),
	        assertion(REJECTED, "is not timed out", task -> !task.isTimedOut()),
            assertion(REJECTED, "is not eligible for execution", task -> !task.isEligibleForExecution()),
	        assertion(TIMEOUT, "is timed out",task -> task.isTimedOut()),
			assertion(TIMEOUT, "is not ready",task -> !task.isReady()),
			assertion(TIMEOUT, "is not active",task -> !task.isActive()),
			assertion(TIMEOUT, "is not terminated",task -> !task.isTerminated()),
			assertion(TIMEOUT, "is not succeeded",task -> !task.isSucceeded()),
			assertion(TIMEOUT, "is not failed",task -> !task.isFailed()),
	        assertion(TIMEOUT, "is not waiting for execution", task -> !task.isWaiting()),
	        assertion(TIMEOUT, "is resumable",task -> task.isResumable()),
	        assertion(TIMEOUT, "is not eligible for execution", task -> !task.isEligibleForExecution()),
            assertion(WAITING, "is not timed out",task -> !task.isTimedOut()),
            assertion(WAITING, "is not ready",task -> !task.isReady()),
            assertion(WAITING, "is not active",task -> !task.isActive()),
            assertion(WAITING, "is not terminated",task -> !task.isTerminated()),
            assertion(WAITING, "is not succeeded",task -> !task.isSucceeded()),
            assertion(WAITING, "is not failed",task -> !task.isFailed()),
            assertion(WAITING, "is waiting for execution", task -> task.isWaiting()),
            assertion(WAITING, "is resumable",task -> task.isResumable())
		});
	}
	
	
	
	private Function<Job_Task,Boolean> assertion;
	private State taskState;
	private String description;
	private Job_Task task;
	public Job_TaskStateTest(State state, String description, Function<Job_Task,Boolean> assertion ) {
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
