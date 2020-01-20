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
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.leitstand.commons.model.ValueObject;
import io.leitstand.inventory.service.ElementGroupId;
import io.leitstand.inventory.service.ElementGroupName;

public class JobSubmission extends ValueObject {

	public static Builder newJobSubmission(){
		return new Builder();
	}

	public static class Builder{
		
		private JobSubmission object = new JobSubmission();

		public Builder withGroupId(ElementGroupId groupId){
			assertNotInvalidated(getClass(), object);
			object.groupId = groupId;
			return this;
		}
		
		public Builder withGroupName(ElementGroupName groupName){
			assertNotInvalidated(getClass(), object);
			object.groupName = groupName;
			return this;
		}
		
		public Builder withJobApplication(JobApplication jobApplication) {
			assertNotInvalidated(getClass(), object);
			object.jobApplication = jobApplication;
			return this;
		}
		
		public Builder withJobName(JobName jobName) {
			assertNotInvalidated(getClass(), object);
			object.jobName = jobName;
			return this;
		}
		
		public Builder withJobType(JobType jobType) {
			assertNotInvalidated(getClass(), object);
			object.jobType = jobType;
			return this;
		}
		
		
		public Builder withTasks(TaskSubmission.Builder... tasks){
			return withTasks(stream(tasks)
				   .map(TaskSubmission.Builder::build)
				   .collect(toList()));
		}

		public Builder withTasks(TaskSubmission... tasks){
			assertNotInvalidated(getClass(), object);
			return withTasks(asList(tasks));
		}
		
		public Builder withTasks(List<TaskSubmission> tasks){
			assertNotInvalidated(getClass(), object);
			object.tasks = new ArrayList<>(tasks);
			if(object.startTaskId == null) {
				object.startTaskId = tasks.get(0).getTaskId();
			}
			return this;
		}
		
		public Builder withStartTaskId(TaskId taskId) {
			assertNotInvalidated(getClass(), object);
			object.startTaskId = taskId;
			return this;
		}

		public Builder withTransitions(TaskTransitionSubmission.Builder... transitions){
			return withTransitions(stream(transitions)
					               .map(TaskTransitionSubmission.Builder::build)
					               .collect(toList()));
		}

		public Builder withTransitions(TaskTransitionSubmission... transitions){
			return withTransitions(asList(transitions));
		}
		
		public Builder withTransitions(List<TaskTransitionSubmission> transitions){
			assertNotInvalidated(getClass(), object);
			object.transitions = new LinkedList<>(transitions);
			return this;
		}
		
		public JobSubmission build(){
			try{
				assertNotInvalidated(getClass(), object);
				return object;
			} finally {
				this.object = null;
			}
		}
		
	}
	
	@JsonbProperty("group_id")
	private ElementGroupId groupId;
	
	@JsonbProperty("group_name")
	private ElementGroupName groupName;
	
	@JsonbProperty("job_name")
	@Valid
	@NotNull(message="{job_name.required}")
	private JobName jobName;
	
	@JsonbProperty("job_application")
	private JobApplication jobApplication;
	
	@JsonbProperty("job_type")
	private JobType jobType;
	
	private List<TaskSubmission> tasks;
	private List<TaskTransitionSubmission> transitions;
	private TaskId startTaskId;
	
	public List<TaskTransitionSubmission> getTransitions() {
		return unmodifiableList(transitions);
	}
	
	public List<TaskSubmission> getTasks() {
		return unmodifiableList(tasks);
	}
	
	public ElementGroupId getGroupId() {
		return groupId;
	}
	
	public JobApplication getJobApplication() {
		return jobApplication;
	}
	
	public JobName getJobName() {
		return jobName;
	}
	
	public JobType getJobType() {
		return jobType;
	}
	
	public ElementGroupName getGroupName() {
		return groupName;
	}
	
	public TaskId getStartTaskId() {
		return startTaskId;
	}
}
