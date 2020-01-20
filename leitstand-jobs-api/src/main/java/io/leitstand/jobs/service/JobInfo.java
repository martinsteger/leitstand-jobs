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

import static io.leitstand.commons.model.BuilderUtil.assertNotInvalidated;
import static java.util.Collections.unmodifiableList;

import java.util.LinkedList;
import java.util.List;

public class JobInfo extends BaseJobEnvelope{

	public static Builder newJobInfo(){
		return new Builder();
	}

	public static class Builder extends BaseJobEnvelopeBuilder<JobInfo, Builder> {
		
		public Builder() {
			super(new JobInfo());
		}

		public Builder withSchedule(JobSchedule schedule){
			assertNotInvalidated(getClass(), object);
			object.schedule = schedule;
			return this;
		}
		
		public Builder withTasks(List<JobTask> tasks){
			assertNotInvalidated(getClass(), object);
			object.tasks = unmodifiableList(new LinkedList<>(tasks));
			return this;
		}
		
		public Builder withProgress(JobProgress progress) {
			assertNotInvalidated(getClass(), object);
			object.progress = progress;
			return this;
		}
		
		public Builder withSchedule(JobSchedule.Builder scheduler) {
			return withSchedule(scheduler.build());
		}

		@Override
		public JobInfo build(){
			try{
				assertNotInvalidated(getClass(), object);
				return object;
			} finally {
				this.object = null;
			}
		}

	}

	private JobProgress progress;
	
	private JobSchedule schedule;
	
	private List<JobTask> tasks;


	public JobSchedule getSchedule() {
		return schedule;
	}
	
	public List<JobTask> getTasks() {
		return unmodifiableList(tasks);
	}
	
	public JobProgress getProgress() {
		return progress;
	}	
	
}
