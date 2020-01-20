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
import static io.leitstand.jobs.service.JobQuery.newJobQuery;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.leitstand.jobs.service.JobService;
import io.leitstand.jobs.service.JobSettings;

@RequestScoped
@Path("/jobs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JobsResource {

	@Inject
	private JobService service;
	
	@GET
	public List<JobSettings> getJobs(@QueryParam("filter") @DefaultValue("") String filter, 
									  @QueryParam("running") @DefaultValue("") String running,
									  @QueryParam("after") String after,
									  @QueryParam("before") String before){
		
		Date scheduledAfter = (after == null || after.isEmpty()) ? parseIsoDate(after) : null;
		Date scheduledBefore = (before == null || before.isEmpty()) ? parseIsoDate(before) : null;
		boolean runningOnly  = Boolean.parseBoolean(running);
		
		return service.findJobs(newJobQuery()
					  .withFilter(filter)
					  .withScheduledAfter(scheduledAfter)
					  .withScheduledBefore(scheduledBefore)
					  .withRunningOnly(runningOnly)
					  .build());
		
	}
	
	
}
