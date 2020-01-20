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
import static io.leitstand.jobs.service.TaskState.FAILED;
import static io.leitstand.security.auth.Role.OPERATOR;
import static io.leitstand.security.auth.Role.SYSTEM;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.leitstand.jobs.flow.TaskUpdateFlow;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobTaskInfo;
import io.leitstand.jobs.service.JobTaskService;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskState;

@RequestScoped
@Path("/jobs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JobTaskResource {
	
	@Inject
	private JobTaskService service;
	
	@Inject
	private TaskUpdateFlow flow;
	
	@PUT
	@Path("/{job_id}/tasks/{task_id}/task_state")
	@RolesAllowed({OPERATOR,SYSTEM})
	public void updateTask(@Valid @PathParam("job_id") JobId jobId, 
	                       @Valid @PathParam("task_id") TaskId taskId,
	                       TaskState state){
		flow.processTask(jobId,
						 taskId, 
						 state);
	}

	
	@PUT
	@Path("/{job_id}/tasks/{task_id}/outcome")
	@RolesAllowed({OPERATOR,SYSTEM})
	public void updateTask(@Valid @PathParam("job_id") JobId jobId, 
	                       @Valid @PathParam("task_id") TaskId taskId,
	                       JsonObject json){
		
		int status = json.getInt("status");
		if(200 <= status && status <= 299){
			flow.processTask(jobId,
							 taskId, 
							 COMPLETED);
		} else {
			flow.processTask(jobId,
							 taskId, 
							 FAILED);
		}
	}
	
	@GET
	@Path("/{job_id}/tasks/{task_id}")
	public JobTaskInfo getTask(@Valid @PathParam("job_id") JobId jobId, 
	                          @Valid @PathParam("task_id") TaskId taskId){
		return service.getJobTask(jobId,taskId);
	}

}
