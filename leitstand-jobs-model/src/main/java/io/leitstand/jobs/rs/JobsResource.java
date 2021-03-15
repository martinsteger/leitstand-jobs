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

import static io.leitstand.commons.jsonb.IsoDateAdapter.parseIsoDate;
import static io.leitstand.jobs.rs.Scopes.JOB;
import static io.leitstand.jobs.rs.Scopes.JOB_READ;
import static io.leitstand.jobs.service.JobQuery.newJobQuery;
import static java.lang.Boolean.parseBoolean;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.leitstand.commons.rs.Resource;
import io.leitstand.jobs.service.JobService;
import io.leitstand.jobs.service.JobSettings;
import io.leitstand.security.auth.Scopes;

@Resource
@Scopes({JOB,JOB_READ})
@Path("/jobs")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class JobsResource {

	@Inject
	private JobService service;
	
	@GET
	public List<JobSettings> getJobs(@QueryParam("filter") @DefaultValue("") String filter, 
									 @QueryParam("running") @DefaultValue("") String running,
									 @QueryParam("after") String after,
									 @QueryParam("before") String before){
		
		Date scheduledAfter = parseIsoDate(after);
		Date scheduledBefore = parseIsoDate(before);
		boolean runningOnly  = parseBoolean(running);
		
		return service.findJobs(newJobQuery()
					            .withFilter(filter)
					            .withScheduledAfter(scheduledAfter)
					            .withScheduledBefore(scheduledBefore)
					            .withRunningOnly(runningOnly)
					            .build());
		
	}
	
	
}
