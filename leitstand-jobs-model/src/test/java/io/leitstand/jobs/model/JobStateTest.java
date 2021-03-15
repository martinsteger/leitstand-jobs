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
import static io.leitstand.jobs.service.State.TIMEOUT;
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
public class JobStateTest {
	
	
	private static Object[] assertion(State state,
									  String message,
									  Function<Job,Boolean> assertion)  {
		return new Object[] {state,message,assertion};
		
	}
 	@Parameters
	public static Collection<Object[]> mappings(){
		return asList(new Object[][] {
		    assertion(NEW, "is new",job -> job.isNew()),
            assertion(NEW, "is not ready", job-> !job.isReady()),
		    assertion(NEW, "is not terminated", job-> !job.isTerminated()),
            assertion(NEW, "is not cancelled", job -> !job.isCancelled()),
            assertion(NEW, "is not completed", job -> !job.isCompleted()),
            assertion(NEW, "is not failed", job -> !job.isFailed()),
            assertion(NEW, "is not suspended", job -> !job.isSuspended()),
            assertion(NEW, "is not timed out", job -> !job.isTimedOut()),
            assertion(READY, "is ready",job -> job.isReady()),
            assertion(READY, "is not terminated", job-> !job.isTerminated()),
            assertion(READY, "is not cancelled", job -> !job.isCancelled()),
            assertion(READY, "is not completed", job -> !job.isCompleted()),
            assertion(READY, "is not failed", job -> !job.isFailed()),
            assertion(READY, "is not suspended", job -> !job.isSuspended()),
            assertion(READY, "is not timed out", job -> !job.isTimedOut()),
            assertion(ACTIVE, "is not ready", job -> !job.isReady()),
		    assertion(ACTIVE, "is running",job -> job.isRunning()),
			assertion(ACTIVE, "is not terminated", job-> !job.isTerminated()),
			assertion(ACTIVE, "is not cancelled", job -> !job.isCancelled()),
			assertion(ACTIVE, "is not completed", job -> !job.isCompleted()),
			assertion(ACTIVE, "is not failed", job -> !job.isFailed()),
			assertion(ACTIVE, "is not suspended", job -> !job.isSuspended()),
			assertion(ACTIVE, "is not ready", job -> !job.isReady()),
	        assertion(CANCELLED,"is cancelled", job -> job.isCancelled()),
			assertion(CANCELLED,"is terminated", job -> job.isTerminated()),
			assertion(CANCELLED,"is not completed", job -> !job.isCompleted()),
			assertion(CANCELLED,"is not running", job -> !job.isRunning()),
			assertion(CANCELLED,"is not failed", job -> !job.isFailed()),
	        assertion(CANCELLED, "is not suspended", job -> !job.isSuspended()),
	        assertion(CANCELLED, "is not timed out", job -> !job.isTimedOut()),
	        assertion(FAILED, "is failed", job -> job.isFailed()),
			assertion(FAILED, "is terminated", job -> job.isTerminated()),
			assertion(FAILED, "is not running", job -> !job.isRunning()),
			assertion(FAILED, "is not completed", job -> !job.isCompleted()),
	        assertion(FAILED, "is not cancelled", job -> !job.isCancelled()),
	        assertion(FAILED, "is not timed out", job -> !job.isTimedOut()),
	        assertion(FAILED, "is not suspended", job -> !job.isSuspended()),
	        assertion(CONFIRM,"is suspended", job -> job.isSuspended()),
			assertion(CONFIRM, "is not running", job -> !job.isRunning()),
			assertion(CONFIRM,"is not terminated", job -> !job.isTerminated()),
	        assertion(CONFIRM, "is not failed", job -> !job.isFailed()),
	        assertion(CONFIRM, "is not timed out", job -> !job.isTimedOut()),
            assertion(CONFIRM, "is not timed completed", job -> !job.isCompleted()),
	        assertion(COMPLETED,"is completed",job -> job.isCompleted()),
			assertion(COMPLETED,"is not failed",job -> !job.isFailed()),
			assertion(COMPLETED,"is not running",job -> !job.isRunning()),
	        assertion(COMPLETED, "is terminated", job -> job.isTerminated()),
	        assertion(COMPLETED, "is not ready", job -> !job.isReady()),
            assertion(COMPLETED, "is not timed out", job -> !job.isTimedOut()),
	        assertion(TIMEOUT,"is timed out",job -> job.isTimedOut()),
			assertion(TIMEOUT,"is not running",job -> !job.isRunning()),
			assertion(TIMEOUT,"is not completed",job -> !job.isCompleted()),
			assertion(TIMEOUT,"is not terminated",job -> !job.isTerminated()),
			assertion(TIMEOUT,"is not suspended",job -> !job.isSuspended()),
			assertion(TIMEOUT,"is not failed",job -> !job.isFailed()),
	        assertion(TIMEOUT, "is ready", job -> !job.isReady())
		});
	}
	
	
	
	private Function<Job,Boolean> assertion;
	private State jobState;
	private String description;
	private Job job;
	public JobStateTest(State state, String description, Function<Job,Boolean> assertion ) {
		this.jobState = state;
		this.assertion = assertion;
		this.description = description;
		this.job = new Job();
	}
	
	@Test
	public void stateMapping() {
		job.setJobState(jobState);
		assertTrue(jobState+" job "+description,assertion.apply(job));
	}

	
}
