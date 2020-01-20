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
package io.leitstand.jobs.rs;

import static io.leitstand.jobs.service.TaskState.COMPLETED;
import static io.leitstand.security.auth.Role.OPERATOR;
import static io.leitstand.security.auth.Role.SYSTEM;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.leitstand.commons.messages.Messages;
import io.leitstand.jobs.flow.JobResumeFlow;
import io.leitstand.jobs.flow.TaskUpdateFlow;
import io.leitstand.jobs.service.JobFlow;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobInfo;
import io.leitstand.jobs.service.JobProgress;
import io.leitstand.jobs.service.JobService;
import io.leitstand.jobs.service.JobSettings;
import io.leitstand.jobs.service.JobSubmission;
import io.leitstand.jobs.service.JobTasks;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskState;

@RequestScoped
@Path("/jobs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JobResource {

	@Inject
	private JobService service;
	
	@Inject
	private Messages messages;
	
	@Inject
	private JobResumeFlow resumeFlow;
	
	@Inject
	private TaskUpdateFlow updateFlow;
	
	
	@GET
	@Path("/{job_id}/submission")
	public JobSubmission getJobSubmission(@PathParam("job_id") JobId jobId){
		return service.getJobSubmission(jobId);
	}
	
	@GET
	@Path("/{job_id}")
	public JobInfo getJobInfo(@PathParam("job_id") JobId jobId){
		return service.getJobInfo(jobId);
	}


	@GET
	@Path("/{job_id}/tasks")
	public JobTasks getTasks(@PathParam("job_id") JobId jobId){
		return service.getJobTasks(jobId);
	}
	
	@PUT
	@Path("/{job_id}")
	@RolesAllowed({OPERATOR,SYSTEM})
	public Messages storeJob(@PathParam("job_id") JobId jobId, JobSubmission submission){
		service.storeJob(jobId,submission);
		return messages;
	}
	
	@DELETE
	@Path("/{job_id}")
	@RolesAllowed({OPERATOR,SYSTEM})
	public Messages removeJob(@PathParam("job_id") JobId jobId){
		service.removeJob(jobId);
		return messages;
	}
	
	@POST
	@Path("/{job_id}/_resume")
	@RolesAllowed({OPERATOR,SYSTEM})
	public void resumeJob(@PathParam("job_id") JobId jobId){
		resumeFlow.resumeJob(jobId);
	}
	
	@POST
	@Path("/{job_id}/_commit")
	@RolesAllowed({OPERATOR,SYSTEM})
	public void commitJob(@PathParam("job_id") JobId jobId){
		service.commitJob(jobId);
	}
	
	@GET
	@Path("/{job_id}/progress")
	public JobProgress getJobProgress( @Valid @PathParam("job_id") JobId jobId){
		return service.getJobProgress(jobId);
	}

	@GET
	@Path("/{job_id}/flow")
	public JobFlow getJobFlow( @Valid @PathParam("job_id") JobId jobId){
		return service.getJobFlow(jobId);
	}
	
	@GET
	@Path("/{job_id}/settings")
	public JobSettings getJobSettings( @Valid @PathParam("job_id") JobId jobId){
		return service.getJobSettings(jobId);
	}
	
	@PUT
	@Path("/{job_id}/settings")
	@RolesAllowed({OPERATOR,SYSTEM})
	public Messages setJobSettings(@Valid @PathParam("job_id") JobId jobId, 
								   @Valid JobSettings settings){
		service.storeJobSettings(jobId, settings);
		return messages;
	}
	
	/**
	 * @deprecated Use {@link #updateJobState(JobId, TaskState)} instead.
	 * @param jobId
	 * @param state
	 */
	@Deprecated(forRemoval=true)
	@PUT
	@Path("/{job_id}/job_state")
	@RolesAllowed({OPERATOR,SYSTEM})
	public void _updateJobState(@Valid @PathParam("job_id") JobId jobId, 
	                           TaskState state){
		service.updateJobState(jobId,state);
	}
	
	
	@PUT
	@Path("/{job_id}/settings/job_state")
	@RolesAllowed({OPERATOR,SYSTEM})
	public void updateJobState(@Valid @PathParam("job_id") JobId jobId, 
	                           TaskState state){
		service.updateJobState(jobId,state);
	}
	
	@POST
	@Path("/{job_id}/_cancel")
	@RolesAllowed({OPERATOR,SYSTEM})
	public void cancelJob(@Valid @PathParam("job_id") JobId jobId){
		service.cancelJob(jobId);
	}
	
	@POST
	@Path("/{job_id}/_confirm")
	@RolesAllowed({OPERATOR,SYSTEM})
	public void confirmJob(@Valid @PathParam("job_id") JobId jobId){
		for(TaskId task : service.confirmJob(jobId)) {
			updateFlow.processTask(jobId,
								   task,
								   COMPLETED);
		};
	}
	
	
}
