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

import javax.json.bind.annotation.JsonbProperty;

import io.leitstand.commons.model.ValueObject;

public class JobProgress extends ValueObject {


	public static Builder newJobProgress() {
		return new Builder();
	}

	public static class Builder{
		
		private JobProgress progress = new JobProgress();
		
		public Builder withReadyCount(int count) {
			this.progress.readyCount = count;
			return this;
		}

		public Builder withActiveCount(int count) {
			this.progress.activeCount = count;
			return this;
		}
		
		public Builder withCompletedCount(int count) {
			this.progress.completedCount = count;
			return this;
		}
		
		public Builder withFailedCount(int count) {
			this.progress.failedCount = count;
			return this;
		}
		
		public Builder withTimeoutCount(int count) {
			this.progress.timeoutCount = count;
			return this;
		}
		
		public JobProgress build() {
			try {
				return this.progress;
			} finally {
				this.progress = null;
			}
		}
	}
	
	@JsonbProperty("ready")
	private int readyCount;
	@JsonbProperty("active")
	private int activeCount;
	@JsonbProperty("completed")
	private int completedCount;
	@JsonbProperty("failed")
	private int failedCount;
	@JsonbProperty("timeout")
	private int timeoutCount;
	
	public int getReadyCount() {
		return readyCount;
	}
	
	public int getActiveCount() {
		return activeCount;
	}
	
	public int getCompletedCount() {
		return completedCount;
	}
	
	public int getFailedCount() {
		return failedCount;
	}
	
	public int getTimeoutCount() {
		return timeoutCount;
	}

	
	
	
	
}
