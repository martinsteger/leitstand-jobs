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

public class JobTaskInfo extends JobTask{

	public static JobTaskInfoBuilder newJobTaskInfo(){
		return new JobTaskInfoBuilder();
	}
	
	public static class JobTaskInfoBuilder extends JobTask.JobTaskBuilder<JobTaskInfo,JobTaskInfoBuilder>{
		
		JobTaskInfoBuilder(){
			super(new JobTaskInfo());
		}
		
		public JobTaskInfoBuilder withJobId(JobId jobId){
			object.jobId = jobId;
			return this;
		}
		
		public JobTaskInfoBuilder withJobName(JobName jobName){
			object.jobName = jobName;
			return this;
		}

		public JobTaskInfoBuilder withJobType(JobType jobType){
			object.jobType = jobType;
			return this;
		}
		
		public JobTaskInfoBuilder withJobApplication(JobApplication jobApplication){
			object.jobApplication = jobApplication;
			return this;
		}
		
	}
	
	
	private JobId jobId;
	
	private JobName jobName;
	
	private JobType jobType;
	
	private JobApplication jobApplication;
	
	public JobId getJobId() {
		return jobId;
	}
	
	public JobName getJobName() {
		return jobName;
	}

	public JobType getJobType() {
		return jobType;
	}

	public JobApplication getJobApplication() {
		return jobApplication;
	}


}
