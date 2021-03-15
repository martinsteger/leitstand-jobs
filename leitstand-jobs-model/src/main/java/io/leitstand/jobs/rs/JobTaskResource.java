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

import static io.leitstand.commons.rs.Responses.success;
import static io.leitstand.jobs.rs.Scopes.JOB;
import static io.leitstand.jobs.rs.Scopes.JOB_READ;
import static io.leitstand.jobs.rs.Scopes.JOB_TASK;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.State.FAILED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.rs.Resource;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobTaskInfo;
import io.leitstand.jobs.service.JobTaskService;
import io.leitstand.jobs.service.State;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.security.auth.Scopes;

@Resource
@Scopes({JOB,JOB_TASK})
@Path("/jobs")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class JobTaskResource {
	
	@Inject
	private JobTaskService service;
	
	@Inject
	private Messages messages;
	
	@PUT
	@Path("/{job_id}/tasks/{task_id}/task_state")
	public void updateTask(@Valid @PathParam("job_id") JobId jobId, 
	                       @Valid @PathParam("task_id") TaskId taskId,
	                       State state){
	    service.updateTask(jobId, 
	                       taskId, 
	                       state);
	}
	
	@PUT
	@Path("/{job_id}/tasks/{task_id}/outcome")
	public void updateTask(@Valid @PathParam("job_id") JobId jobId, 
	                       @Valid @PathParam("task_id") TaskId taskId,
	                       JsonObject json){
		
		int status = json.getInt("status");
		if(200 <= status && status <= 299){
			service.updateTask(jobId,
							   taskId, 
							   COMPLETED);
		} else {
			service.updateTask(jobId,
							   taskId, 
							   FAILED);
		}
	}
	
   @PUT
   @Path("/{job_id}/tasks/{task_id}/parameters")
   public Response setTaskParameters(@Valid @PathParam("job_id") JobId jobId, 
                                     @Valid @PathParam("task_id") TaskId taskId,
                                     JsonObject parameters){
        
       service.setTaskParameter(jobId, taskId, parameters);
       return success(messages);
    }
	
	@GET
	@Scopes({JOB,JOB_READ,JOB_TASK})
	@Path("/{job_id}/tasks/{task_id}")
	public JobTaskInfo getTask(@Valid @PathParam("job_id") JobId jobId, 
	                          @Valid @PathParam("task_id") TaskId taskId){
		return service.getJobTask(jobId,taskId);
	}

}
