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

import java.util.UUID;

import javax.json.bind.annotation.JsonbTypeAdapter;

import io.leitstand.commons.model.Scalar;
import io.leitstand.jobs.jsonb.JobIdAdapter;

@JsonbTypeAdapter(JobIdAdapter.class)
public class JobId extends Scalar<String> {

	private static final long serialVersionUID = 1L;

	public static final JobId randomJobId(){
		return new JobId(UUID.randomUUID().toString());
	}
	
	public static final JobId jobId(String id) {
		return valueOf(id);
	}

	/**
	 * Creates an <code>JobId</code> from the specified string.
	 * @param id the job ID
	 * @return the <code>JobId</code> or <code>null</code> if the specified string is <code>null</code> or empty.
	 */
	public static JobId valueOf(String id) {
		return fromString(id,JobId::new);
	}
	
	private String value;
	
	public JobId(){
		// JPA
	}
	
	public JobId(String value){
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return value;
	}

}
