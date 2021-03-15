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

import io.leitstand.commons.model.ValueObject;
import io.leitstand.security.auth.UserName;

public class JobSettings extends ValueObject{

	public static Builder newJobSettings(){
		return new Builder();
	}

	public static class Builder {
		private JobSettings job = new JobSettings();
		
		public Builder withJobApplication(JobApplication application) {
			job.jobApplication = application;
			return this;
		}
		
		public Builder withJobType(JobType type) {
			job.jobType = type;
			return this;
		}
		
		public Builder withJobName(JobName name){
			job.jobName = name;
			return this;
		}
		
		public Builder withJobId(JobId jobId){
			job.jobId = jobId;
			return this;
		}
		
		public Builder withJobState(State state){
			job.jobState = state;
			return this;
		}
		
		public Builder withJobOwner(UserName owner) {
		    job.jobOwner = owner;
		    return this;
		}

		public Builder withSchedule(JobSchedule schedule){
			job.schedule = schedule;
			return this;
		}
		
		public Builder withDateModified(Date date) {
			job.dateModified = new Date(date.getTime());
			return this;
		}
		
		public JobSettings build(){
			try{
				return job;
			} finally {
				this.job = null;
			}
		}


	}

	private JobId jobId;
	
	private State jobState;
	
	private JobName jobName;
	
	private JobType jobType;
	
	private JobApplication jobApplication;
		
	private UserName jobOwner;
	
	private JobSchedule schedule;
	
	@JsonbProperty("date_modified")
	private Date dateModified;
	
	public JobId getJobId() {
		return jobId;
	}

	public State getJobState() {
		return jobState;
	}
	
	public JobType getJobType() {
		return jobType;
	}
	
	public JobApplication getJobApplication() {
		return jobApplication;
	}

	public JobName getJobName() {
		return jobName;
	}
	
	public JobSchedule getSchedule() {
		return schedule;
	}

	public UserName getJobOwner() {
        return jobOwner;
    }
	
	public Date getDateModified() {
		if(dateModified == null) {
			return null;
		}
		return new Date(dateModified.getTime());
	}
	
}
