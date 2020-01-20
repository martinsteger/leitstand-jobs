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

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.leitstand.commons.model.Scalar;
import io.leitstand.jobs.jsonb.JobNameAdapter;

@JsonbTypeAdapter(JobNameAdapter.class)
public class JobName extends Scalar<String>{

	private static final long serialVersionUID = 1L;

	
	public static JobName jobName(Scalar<String> name) {
		return valueOf(name);
	}
	
	public static JobName jobName(String name) {
		return valueOf(name);
	}

	public static JobName valueOf(Scalar<String> name) {
		return valueOf(Scalar.toString(name));
	}

	
	public static JobName valueOf(String name) {
		return fromString(name,JobName::new);
	}
	
	@NotNull(message="{job_name.required}")
	@Pattern(message="{job_name.invalid}", regexp="\\p{Print}{1,64}")
	private String value;
	
	public JobName(String value){
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return value;
	}

}
