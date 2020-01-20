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

import static io.leitstand.jobs.service.TaskState.READY;

import io.leitstand.jobs.service.TaskState;

public class JobStateChangedEvent implements StateChangedEvent {

	private Job job;

	public JobStateChangedEvent(Job job){
		this.job = job;
	}
	
	public Job getJob() {
		return job;
	}
	
	@Override
	public boolean isTimedOut(){
		return job.isTimedOut();
	}
	
	public boolean isTerminated(){
		return job.isTerminated();
	}
	
	public boolean isSubmitted(){
		return job.isSubmitted();
	}

	@Override
	public boolean isFailed(){
		return job.isFailed();
	}

	@Override
	public boolean isCompleted(){
		return job.isCompleted();
	}

	@Override
	public boolean isCancelled() {
		return job.isCancelled();
	}

	@Override
	public boolean isRejected() {
		return job.isInState(TaskState.REJECTED);
	}

	@Override
	public boolean isActive() {
		return job.isRunning();
	}

	@Override
	public boolean isReady() {
		return job.isInState(READY);
	}
	
}
