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

import io.leitstand.commons.model.Scalar;
import io.leitstand.jobs.jsonb.TaskTypeAdapter;

@JsonbTypeAdapter(TaskTypeAdapter.class)
public class TaskType extends Scalar<String> {

	private static final long serialVersionUID = 1L;

	public static TaskType valueOf(String type) {
		return Scalar.fromString(type,TaskType::new);
	}
	
	private String value;
	
	public TaskType(String value) {
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return value;
	}

}
