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

import java.util.Date;

import io.leitstand.commons.model.ValueObject;

public class JobQuery extends ValueObject{

	public static Builder newJobQuery() {
		return new Builder();
	}
	
	public static class Builder {
		private JobQuery query = new JobQuery();
		
		public Builder withFilter(String filter) {
			query.filter = filter;
			return this;
		}
		
		public Builder withRunningOnly(boolean runningOnly) {
			query.runningOnly = runningOnly;
			return this;
		}
		
		public Builder withScheduledAfter(Date after) {
			query.scheduledAfter = after;
			return this;
		}
		
		public Builder withScheduledBefore(Date before) {
			query.scheduledBefore = before;
			return this;
		}
		
		public JobQuery build() {
			try {
				return query;
			} finally {
				this.query = null;
			}
		}
	}
	
	private String filter;
	private boolean runningOnly;
	private Date scheduledAfter;
	private Date scheduledBefore;

	public String getFilter() {
		return filter;
	}
	
	public boolean isRunningOnly() {
		return runningOnly;
	}
	
	public Date getScheduledAfter() {
		if(scheduledAfter == null) {
			return null;
		}
		return new Date(scheduledAfter.getTime());
	}
	
	public Date getScheduledBefore() {
		if(scheduledBefore == null) {
			return null;
		}
		return new Date(scheduledBefore.getTime());
	}
	
}
