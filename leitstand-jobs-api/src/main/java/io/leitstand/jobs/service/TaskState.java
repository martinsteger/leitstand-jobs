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

public enum TaskState {
	NEW(false),
	READY(false),
	ACTIVE(false),
	FAILED(true),
	CANCELLED(true),
	SKIPPED(true),
	CONFIRM(false),
	COMPLETED(true),
	TIMEOUT(false),
	REJECTED(true);
	
	private TaskState(boolean terminal){
		this.terminal = terminal;
	}
	
	private boolean terminal;
	
	public boolean isTerminalState(){
		return terminal;
	}

	public static TaskState taskState(String state) {
		return valueOf(state);
	}
	
}
