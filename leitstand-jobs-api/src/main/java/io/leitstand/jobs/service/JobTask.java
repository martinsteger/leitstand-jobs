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

import static io.leitstand.commons.model.BuilderUtil.assertNotInvalidated;

import java.util.Date;

import javax.json.JsonObject;

import io.leitstand.commons.model.ValueObject;
import io.leitstand.inventory.service.ElementAlias;
import io.leitstand.inventory.service.ElementGroupId;
import io.leitstand.inventory.service.ElementGroupName;
import io.leitstand.inventory.service.ElementGroupType;
import io.leitstand.inventory.service.ElementId;
import io.leitstand.inventory.service.ElementName;
import io.leitstand.inventory.service.ElementRoleName;

public class JobTask extends ValueObject{
	
	public static Builder newJobTask() {
		return new Builder();
	}

	public static class JobTaskBuilder<T extends JobTask, B extends JobTaskBuilder<T,B>> {

		protected T object;
		
		JobTaskBuilder(T object){
			this.object = object;
		}
		
		public B withTaskId(TaskId taskId) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).taskId = taskId;
			return (B) this;
		}
		
		public B withTaskName(TaskName taskName) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).taskName = taskName;
			return (B) this;
		}
		
		public B withTaskType(TaskType taskType) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).taskType = taskType;
			return (B) this;
		}
		
		public B withElementId(ElementId elementId) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).elementId = elementId;
			return (B) this;
		}
		
		public B withElementName(ElementName elementName) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).elementName = elementName;
			return (B) this;
		}
		
		public B withElementAlias(ElementAlias elementAlias) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).elementAlias = elementAlias;
			return (B) this;
		}
		
		public B withElementRole(ElementRoleName elementRole) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).elementRole = elementRole;
			return (B) this;
		}

		public B withGroupId(ElementGroupId groupId) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).groupId = groupId;
			return (B) this;
		}
		
		public B withGroupName(ElementGroupName groupName) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).groupName = groupName;
			return (B) this;
		}
		
		public B withGroupType(ElementGroupType groupType) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).groupType = groupType;
			return (B) this;
		}
		
		
		public B withTaskState(State taskState) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).taskState = taskState;
			return (B) this;
		}
		
		public B withCanary(boolean canary) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).canary = canary;
			return (B) this;
		}
		
		public B withParameter(JsonObject parameters) {
			assertNotInvalidated(getClass(), object);
			((JobTask)object).parameters = parameters;
			return (B) this;
		}
		
		public B withDateLastModified(Date date) {
			assertNotInvalidated(getClass(),object);
			((JobTask)object).dateModified = new Date(date.getTime());
			return (B) this;
		}
		
		public T build() {
			try {
				assertNotInvalidated(getClass(), object);
				return this.object;
			} finally {
				this.object = null;
			}
		}

	}

	
	public static class Builder extends JobTaskBuilder<JobTask,Builder>{

		Builder() {
			super(new JobTask());
		}
	}
	
	private TaskId taskId;

	private TaskName taskName;
	
	private TaskType taskType;
	
	private ElementGroupId groupId;
	
	private ElementGroupName groupName;
	
	private ElementGroupType groupType;
	
	private ElementId elementId;

	private ElementName elementName;
	
	private ElementAlias elementAlias;
	
	private ElementRoleName elementRole;
	
	private State taskState;
	
	private Date dateModified;

	private boolean canary;
	
	private JsonObject parameters;

	public TaskId getTaskId() {
		return taskId;
	}
	
	public ElementGroupId getGroupId() {
		return groupId;
	}
	
	public ElementGroupName getGroupName() {
		return groupName;
	}
	
	public ElementGroupType getGroupType() {
		return groupType;
	}

	public ElementId getElementId() {
		return elementId;
	}

	public ElementName getElementName() {
		return elementName;
	}

	public TaskName getTaskName() {
		return taskName;
	}
	
	public TaskType getTaskType() {
		return taskType;
	}

	public State getTaskState() {
		return taskState;
	}
	
	public boolean isCanary() {
		return canary;
	}
	
	public JsonObject getParameters() {
		return parameters;
	}
	
	public ElementAlias getElementAlias() {
		return elementAlias;
	}
	
	public ElementRoleName getElementRole() {
		return elementRole;
	}
	
	public Date getDateModified() {
		if(dateModified != null) {
			return new Date(dateModified.getTime());
		}
		return null;
	}
}
