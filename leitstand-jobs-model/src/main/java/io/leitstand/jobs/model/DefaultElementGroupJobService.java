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
package io.leitstand.jobs.model;

import static io.leitstand.jobs.model.Job.findReadyAndActiveJobsForElementGroup;
import static io.leitstand.jobs.service.ElementGroupJobSummary.newElementGroupJobSummary;
import static io.leitstand.jobs.service.ElementGroupJobs.newElementGroupJobs;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import io.leitstand.commons.model.Repository;
import io.leitstand.commons.model.Service;
import io.leitstand.inventory.service.ElementGroupId;
import io.leitstand.inventory.service.ElementGroupName;
import io.leitstand.inventory.service.ElementGroupSettings;
import io.leitstand.inventory.service.ElementGroupType;
import io.leitstand.jobs.service.ElementGroupJobService;
import io.leitstand.jobs.service.ElementGroupJobSummary;
import io.leitstand.jobs.service.ElementGroupJobs;

@Service
public class DefaultElementGroupJobService implements ElementGroupJobService {
	
	@Inject
	private InventoryClient inventory;
	
	@Inject
	@Jobs
	private Repository repository;
	
	@Override
	public ElementGroupJobs getActiveElementGroupJobs(ElementGroupId id) {
		ElementGroupSettings group = inventory.getGroupSettings(id);
		return getActiveElementGroupJobs(group);
	}

	@Override
	public ElementGroupJobs getActiveElementGroupJobs(ElementGroupType groupType,
													  ElementGroupName groupName) {
		ElementGroupSettings group = inventory.getGroupSettings(groupType,
																groupName);
		return getActiveElementGroupJobs(group);
	}
	
	ElementGroupJobs getActiveElementGroupJobs(ElementGroupSettings group) {
		List<ElementGroupJobSummary> jobs = new LinkedList<>();
		for(Job job : repository.execute(findReadyAndActiveJobsForElementGroup(group.getGroupId()))){
			jobs.add(newElementGroupJobSummary()
					 .withJobId(job.getJobId())
					 .withJobName(job.getJobName())
					 .withJobOwner(job.getJobOwner())
					 .withTaskState(job.getJobState())
					 .withStartDate(job.getDateScheduled())
					 .build());
		}
		
		return newElementGroupJobs()
			   .withGroupId(group.getGroupId())
			   .withGroupName(group.getGroupName())
			   .withJobs(jobs)
			   .build();
		
	}


}
