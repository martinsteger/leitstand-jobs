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

import javax.json.JsonObject;
import javax.json.bind.annotation.JsonbProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.leitstand.commons.model.ValueObject;
import io.leitstand.inventory.service.ElementId;
import io.leitstand.inventory.service.ElementName;

public class TaskSubmission extends ValueObject{

	public static Builder newTaskSubmission(){
		return new Builder();
	}
	
	public static class Builder {
		private TaskSubmission task;		
		
		public Builder(){
			this.task = new TaskSubmission();
		}
		
		public Builder withTaskId(TaskId taskId){
			task.taskId = taskId;
			return this;
		}

		public Builder withTaskType(TaskType taskType){
			task.taskType = taskType;
			return this;
		}
		
		public Builder withTaskName(TaskName taskName){
			task.taskName = taskName;
			return this;
		}
		
		public Builder withElementId(ElementId id){
			task.elementId = id;
			return this;
		}
		
		public Builder withElementName(ElementName name){
			task.elementName = name;
			return this;
		}
		
		public Builder withCanary(boolean canary){
			task.canary = canary;
			return this;
		}
		
		public Builder withParameter(JsonObject parameter){
			task.parameter = parameter;
			return this;
		}
		
		public TaskSubmission build(){
			try{
				return task;
			} finally {
				this.task = null;
			}
		}
		
	}
	
	@JsonbProperty("task_id")
	@Valid
	@NotNull(message="{task_id.required}")
	private TaskId taskId;
	
	@JsonbProperty("task_type")
	@Valid
	@NotNull(message="{task_type.required}")
	private TaskType taskType;
	
	@JsonbProperty("task_name")
	@Valid
	@NotNull(message="{task_name.required}")
	private TaskName taskName;
	
	
	@JsonbProperty("element_id")
	@Valid
	private ElementId elementId;
	
	@JsonbProperty("element_name")
	@Valid
	private ElementName elementName;
	
	private JsonObject parameter;
	
	private boolean canary;
	
	public TaskType getTaskType() {
		return taskType;
	}
	
	public TaskName getTaskName() {
		return taskName;
	}
	
	public TaskId getTaskId() {
		return taskId;
	}
	public ElementId getElementId() {
		return elementId;
	}
	public ElementName getElementName() {
		return elementName;
	}
	
	public boolean isCanary() {
		return canary;
	}
	
	public JsonObject getParameter() {
		return parameter;
	}

}
