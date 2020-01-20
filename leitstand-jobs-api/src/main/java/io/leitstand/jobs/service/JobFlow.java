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


public class JobFlow extends BaseJobEnvelope {

	public static Builder newJobFlow() {
		return new Builder();
	}
	
	public static class Builder extends BaseJobEnvelopeBuilder<JobFlow, Builder>{
		
		public Builder() {
			super(new JobFlow());
		}
		
		public Builder withGraph(String flow) {
			assertNotInvalidated(getClass(), object);
			object.graph = flow;
			return this;
		}
	}
	
	private String graph;

	public String getGraph() {
		return graph;
	}

}
