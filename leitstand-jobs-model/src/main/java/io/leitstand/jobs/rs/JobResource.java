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

import static io.leitstand.jobs.rs.Scopes.JOB;
import static io.leitstand.jobs.rs.Scopes.JOB_READ;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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

import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.rs.Resource;
import io.leitstand.jobs.service.JobFlow;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobInfo;
import io.leitstand.jobs.service.JobProgress;
import io.leitstand.jobs.service.JobService;
import io.leitstand.jobs.service.JobSettings;
import io.leitstand.jobs.service.JobSubmission;
import io.leitstand.jobs.service.JobTasks;
import io.leitstand.jobs.service.State;
import io.leitstand.security.auth.Scopes;

@Resource
@Scopes({JOB})
@Path("/jobs")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class JobResource {

	@Inject
	private JobService service;
	
	@Inject
	private Messages messages;
	
	@GET
	@Path("/{job_id}")
	@Scopes({JOB,JOB_READ})
	public JobInfo getJobInfo(@PathParam("job_id") JobId jobId){
		return service.getJobInfo(jobId);
	}


	@GET
	@Path("/{job_id}/tasks")
	@Scopes({JOB,JOB_READ})
	public JobTasks getTasks(@PathParam("job_id") JobId jobId){
		return service.getJobTasks(jobId);
	}
	
	@PUT
	@Path("/{job_id}")
	public Messages storeJob(@PathParam("job_id") JobId jobId, JobSubmission submission){
		service.storeJob(jobId,submission);
		return messages;
	}
	
	@DELETE
	@Path("/{job_id}")
	public Messages removeJob(@PathParam("job_id") JobId jobId){
		service.removeJob(jobId);
		return messages;
	}
	
	@POST
	@Path("/{job_id}/_resume")
	public void resumeJob(@PathParam("job_id") JobId jobId){
		service.resumeJob(jobId);
	}
	
	@POST
	@Path("/{job_id}/_commit")
	public void commitJob(@PathParam("job_id") JobId jobId){
		service.commitJob(jobId);
	}
	
	@GET
	@Path("/{job_id}/progress")
	@Scopes({JOB,JOB_READ})
	public JobProgress getJobProgress( @Valid @PathParam("job_id") JobId jobId){
		return service.getJobProgress(jobId);
	}

	@GET
	@Path("/{job_id}/flow")
	@Scopes({JOB,JOB_READ})
	public JobFlow getJobFlow( @Valid @PathParam("job_id") JobId jobId){
		return service.getJobFlow(jobId);
	}
	
	@GET
	@Path("/{job_id}/settings")
	@Scopes({JOB,JOB_READ})
	public JobSettings getJobSettings( @Valid @PathParam("job_id") JobId jobId){
		return service.getJobSettings(jobId);
	}
	
	@PUT
	@Path("/{job_id}/settings")
	public Messages setJobSettings(@Valid @PathParam("job_id") JobId jobId, 
								   @Valid JobSettings settings){
		service.storeJobSettings(jobId, settings);
		return messages;
	}
	
	/**
	 * @deprecated Use {@link #updateJobState(JobId, State)} instead.
	 * @param jobId
	 * @param state
	 */
	@Deprecated(forRemoval=true)
	@PUT
	@Path("/{job_id}/job_state")
	public void _updateJobState(@Valid @PathParam("job_id") JobId jobId, 
	                           State state){
		service.updateJobState(jobId,state);
	}
	
	
	@PUT
	@Path("/{job_id}/settings/job_state")
	public void updateJobState(@Valid @PathParam("job_id") JobId jobId, 
	                           State state){
		service.updateJobState(jobId,state);
	}
	
	@POST
	@Path("/{job_id}/_cancel")
	public void cancelJob(@Valid @PathParam("job_id") JobId jobId){
		service.cancelJob(jobId);
	}
	
	@POST
	@Path("/{job_id}/_confirm")
	public void confirmJob(@Valid @PathParam("job_id") JobId jobId){
	    service.confirmJob(jobId);
	}
	
	
}
