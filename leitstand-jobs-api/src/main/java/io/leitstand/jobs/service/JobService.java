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

import java.util.List;

public interface JobService {
	
	void cancelJob(JobId jobId);
	
	void commitJob(JobId jobId);
	
	List<TaskId> confirmJob(JobId jobId);
	
	List<JobSettings> findJobs(JobQuery query);
	
	JobFlow getJobFlow(JobId jobId);
	
	JobInfo getJobInfo(JobId id);
	
	JobProgress getJobProgress(JobId id);
	
	JobSettings getJobSettings(JobId jobId);
	
	JobSubmission getJobSubmission(JobId jobId);
	
	JobTasks getJobTasks(JobId jobId);
	
	void removeJob(JobId jobId);
	
	List<TaskId> resumeJob(JobId jobId);
	
	void storeJob(JobId jobId, JobSubmission submission);

	void storeJobSettings(JobId jobId, JobSettings settings);
	
	void updateJobState(JobId jobId, TaskState state);

}
