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
package io.leitstand.jobs.service;

public enum State {
	/** A new task of a new process. The task is not eligible for execution.*/
    NEW(false),
    /** A task waiting for other tasks to complete. The task is not eligible for execution.*/
	WAITING(false),
	/** A task ready for execution.*/
	READY(false),
	/** A task that is currently being processed.*/
	ACTIVE(false),
	/** A failed task.*/
	FAILED(true),
	/** A cancelled task.*/
	CANCELLED(true),
	/** A skipped task.*/
	SKIPPED(true),
	/** Task is done and waits for confirmation to complete.*/
	CONFIRM(false),
	/** Task is completed, i.e. task is done and all waiting successors are marked ready.*/
	COMPLETED(true),
	/** A active task was not completed in time. This is an information state. The task can still change to any terminal state or DONE state, but it was not <i>done</i> in the specified time range.*/
	TIMEOUT(false),
	/** Task has been rejected and cannot be executed.*/
	REJECTED(true);
	
	private State(boolean terminal){
		this.terminal = terminal;
	}
	
	private boolean terminal;
	
	public boolean isTerminalState(){
		return terminal;
	}

	public static State taskState(String state) {
		return valueOf(state);
	}
	
}
