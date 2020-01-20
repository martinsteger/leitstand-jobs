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

import javax.json.bind.annotation.JsonbProperty;

import io.leitstand.commons.model.ValueObject;
import io.leitstand.inventory.service.ElementGroupId;
import io.leitstand.inventory.service.ElementGroupName;
import io.leitstand.security.auth.UserName;


public class BaseJobEnvelope extends ValueObject {

	public static class BaseJobEnvelopeBuilder<T extends BaseJobEnvelope, B extends BaseJobEnvelope.BaseJobEnvelopeBuilder<T,B>>{
		
		protected T object;
		
		protected BaseJobEnvelopeBuilder(T object) {
			this.object = object;
		}
		
		public B withGroupId(ElementGroupId groupId) {
			assertNotInvalidated(getClass(), object);
			((BaseJobEnvelope)object).groupId = groupId;
			return (B) this;
		}
		
		public B withGroupName(ElementGroupName groupName) {
			assertNotInvalidated(getClass(), object);
			((BaseJobEnvelope)object).groupName = groupName;
			return (B) this;
		}
		
		public B withJobApplication(JobApplication application) {
			assertNotInvalidated(getClass(), object);
			((BaseJobEnvelope)object).jobApplication = application;
			return (B) this;
		}
		
		public B withJobType(JobType type) {
			assertNotInvalidated(getClass(), object);
			((BaseJobEnvelope)object).jobType = type;
			return (B) this;
		}
		
		public B withJobName(JobName name) {
			assertNotInvalidated(getClass(), object);
			((BaseJobEnvelope)object).jobName = name;
			return (B) this;
		}
		
		public B withJobId(JobId jobId) {
			assertNotInvalidated(getClass(), object);
			((BaseJobEnvelope)object).jobId = jobId;
			return (B) this;
		}
		
		public B withJobState(TaskState state) {
			assertNotInvalidated(getClass(), object);
			((BaseJobEnvelope)object).jobState = state;
			return (B) this;
		}
		
		public B withJobOwner(UserName userName) {
			assertNotInvalidated(getClass(), object);
			((BaseJobEnvelope)object).jobOwner = userName;
			return (B) this;
		}
		
		public T build() {
			try {
				assertNotInvalidated(getClass(), object);
				return object;
			} finally {
				object = null;
			}
		}
		
	}
	
	
	@JsonbProperty("group_id")
	private ElementGroupId groupId;

	@JsonbProperty("group_name")
	private ElementGroupName groupName;

	@JsonbProperty("job_id")
	private JobId jobId;

	@JsonbProperty("job_name")
	private JobName jobName;

	@JsonbProperty("job_type")
	private JobType jobType;
	
	@JsonbProperty("job_application")
	private JobApplication jobApplication;
	
	@JsonbProperty("job_owner")
	private UserName jobOwner;
	@JsonbProperty("job_state")
	private TaskState jobState;

	public JobId getJobId() {
		return jobId;
	}

	public JobName getJobName() {
		return jobName;
	}

	public UserName getJobOwner() {
		return jobOwner;
	}

	public TaskState getJobState() {
		return jobState;
	}
	
	public JobApplication getJobApplication() {
		return jobApplication;
	}
	
	public JobType getJobType() {
		return jobType;
	}

	public ElementGroupId getGroupId() {
		return groupId;
	}
	
	public ElementGroupName getGroupName() {
		return groupName;
	}
	
}
