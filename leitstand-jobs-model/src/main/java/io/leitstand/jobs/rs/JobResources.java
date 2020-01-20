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

import static io.leitstand.commons.model.ObjectUtil.asSet;

import java.util.Set;

import javax.enterprise.context.Dependent;

import io.leitstand.commons.rs.ApiResourceProvider;
import io.leitstand.jobs.jsonb.JobApplicationAdapter;
import io.leitstand.jobs.jsonb.JobIdAdapter;
import io.leitstand.jobs.jsonb.JobNameAdapter;
import io.leitstand.jobs.jsonb.JobTypeAdapter;
import io.leitstand.jobs.jsonb.TaskIdAdapter;
import io.leitstand.jobs.jsonb.TaskNameAdapter;
import io.leitstand.jobs.jsonb.TaskTypeAdapter;

/**
 * Provider of all scheduler module resources.
 */
@Dependent
public class JobResources implements ApiResourceProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Class<?>> getResources() {
		return asSet(JobsResource.class,
					 JobResource.class,
					 JobTaskResource.class,
					 ElementGroupJobResource.class,
					 JobApplicationAdapter.class,
					 JobIdAdapter.class,
					 JobNameAdapter.class,
					 JobTypeAdapter.class,
					 TaskIdAdapter.class,
					 TaskNameAdapter.class,
					 TaskTypeAdapter.class);
	}

}
