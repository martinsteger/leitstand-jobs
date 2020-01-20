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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.leitstand.commons.model.ValueObject;

public class TaskTransitionSubmission extends ValueObject {

	public static Builder newTaskTransitionSubmission(){
		return new Builder();
	}
	
	public static class Builder {
		
		private TaskTransitionSubmission transition = new TaskTransitionSubmission();
		
		public Builder from(TaskId from){
			transition.from = from;
			return this;
		}
		
		public Builder to(TaskId to){
			transition.to = to;
			return this;
		}
		
		public Builder withName(String name){
			transition.name = name;
			return this;
		}
		
		public TaskTransitionSubmission build(){
			try{
				return transition;
			} finally {
				this.transition = null;
			}
		}
	}
	
	@Valid
	@NotNull(message="{from.required}")
	private TaskId from;
	
	@Valid
	@NotNull(message="{to.required}")
	private TaskId to;
	
	@Size(max=64, message="{name.too-long}")
	private String name;
	
	public TaskId getFrom() {
		return from;
	}
	
	public TaskId getTo() {
		return to;
	}
	
	public String getName() {
		return name;
	}
}
