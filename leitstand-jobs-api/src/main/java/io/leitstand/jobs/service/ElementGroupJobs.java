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

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import javax.json.bind.annotation.JsonbProperty;

import io.leitstand.commons.model.ValueObject;
import io.leitstand.inventory.service.ElementGroupId;
import io.leitstand.inventory.service.ElementGroupName;


public class ElementGroupJobs extends ValueObject {

	public static Builder newElementGroupJobs(){
		return new Builder();
	}
	
	public static class Builder {
		
		private ElementGroupJobs jobs = new ElementGroupJobs();
		
		public Builder withGroupId(ElementGroupId groupId){
			jobs.groupId = groupId;
			return this;
		}
		
		public Builder withGroupName(ElementGroupName groupName){
			jobs.groupName = groupName;
			return this;
		}

		public Builder withJobs(List<ElementGroupJobSummary> jobs){
			this.jobs.jobs = unmodifiableList(new ArrayList<>(jobs));
			return this;
		}
		
		public ElementGroupJobs build(){
			try{
				return jobs;
			} finally {
				this.jobs = null;
			}
		}
		
	}
	
	@JsonbProperty("group_id")
	private ElementGroupId groupId;
	
	@JsonbProperty("group_name")
	private ElementGroupName groupName;
	
	private List<ElementGroupJobSummary> jobs;
	
	
	public ElementGroupId getGroupId() {
		return groupId;
	}
	
	public ElementGroupName getGroupName() {
		return groupName;
	}
	
	public List<ElementGroupJobSummary> getJobs() {
		return unmodifiableList(jobs);
	}
	
}
