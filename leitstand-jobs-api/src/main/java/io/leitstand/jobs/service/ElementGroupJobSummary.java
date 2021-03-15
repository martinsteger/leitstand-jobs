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

import java.util.Date;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTypeAdapter;

import io.leitstand.commons.jsonb.IsoDateAdapter;
import io.leitstand.commons.model.ValueObject;
import io.leitstand.security.auth.UserName;

public class ElementGroupJobSummary extends ValueObject {
	
	public static Builder newElementGroupJobSummary(){
		return new Builder();
	}
	
	public static class Builder {
		private ElementGroupJobSummary job = new ElementGroupJobSummary();
		
		public Builder withJobId(JobId jobId){
			job.jobId = jobId;
			return this;
		}

		public Builder withJobName(JobName name){
			job.jobName = name;
			return this;
		}

		public Builder withJobState(State state){
			job.jobState = state;
			return this;
		}

		public Builder withJobOwner(UserName userName){
			job.jobOwner = userName;
			return this;
		}

		public Builder withStartDate(Date startDate){
			job.startDate = startDate;
			return this;
		}
		
		public ElementGroupJobSummary build(){
			try{
				return job;
			} finally {
				this.job = null;
			}
		}
		
	}
	
	private JobId jobId;
	
	private JobName jobName;
	
	private UserName jobOwner;

	private State jobState;
	
	@JsonbProperty("date_scheduled")
	@JsonbTypeAdapter(IsoDateAdapter.class)
	private Date startDate;
	
	public JobId getJobId() {
		return jobId;
	}
	
	public JobName getJobName() {
		return jobName;
	}

	public State getJobState() {
		return jobState;
	}
	
	public UserName getJobOwner() {
		return jobOwner;
	}
	
	public Date getStartDate() {
		return new Date(startDate.getTime());
	}
	
}
