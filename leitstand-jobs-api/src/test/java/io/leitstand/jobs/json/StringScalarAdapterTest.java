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
package io.leitstand.jobs.json;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;

import javax.json.bind.adapter.JsonbAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.leitstand.commons.model.Scalar;
import io.leitstand.jobs.jsonb.JobIdAdapter;
import io.leitstand.jobs.jsonb.JobNameAdapter;
import io.leitstand.jobs.jsonb.JobTypeAdapter;
import io.leitstand.jobs.jsonb.TaskIdAdapter;
import io.leitstand.jobs.jsonb.TaskNameAdapter;
import io.leitstand.jobs.jsonb.TaskTypeAdapter;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobName;
import io.leitstand.jobs.service.JobType;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskName;
import io.leitstand.jobs.service.TaskType;

@RunWith(Parameterized.class)
public class StringScalarAdapterTest {

	@Parameters
	public static Collection<Object[]> adapters(){
		String uuid = randomUUID().toString();
		return asList(new Object[][]{
			{new JobIdAdapter(),  	     uuid,		   new JobId(uuid)},
			{new JobNameAdapter(),       "job-name",   new JobName("job-name")},
			{new JobTypeAdapter(),       "job-type",   new JobType("job-type")},
			{new TaskIdAdapter(),	     uuid,		   new TaskId(uuid)},
			{new TaskNameAdapter(),      "task-name",  new TaskName("task-name")},
			{new TaskTypeAdapter(),      "task-type",  new TaskType("task-type")}
		});
	}
	
	private JsonbAdapter<Scalar<String>,String> adapter;
	private Scalar<String> scalar;
	private String string;
	
	public StringScalarAdapterTest(JsonbAdapter<Scalar<String>,String> adapter,
								  String string,
								  Scalar<String> scalar) {
		this.adapter = adapter;
		this.string = string;
		this.scalar = scalar;
		
	}
	
	@Test
	public void empty_string_is_mapped_to_null() throws Exception {
		assertNull(adapter.adaptFromJson(""));
	}
	
	@Test
	public void null_string_is_mapped_to_null() throws Exception {
		assertNull(adapter.adaptFromJson(null));
	}
	
	@Test
	public void valid_string_is_adaptFromJsonled_properly() throws Exception{
		assertEquals(scalar,adapter.adaptFromJson(string));
	}
	
	@Test
	public void non_null_scalar_is_marshalled_properly() throws Exception {
		assertEquals(string,adapter.adaptToJson(scalar));
	}
	
	@Test
	public void null_scalar_is_mapped_to_null() throws Exception{
		assertNull(adapter.adaptToJson(null));
	}
	
}
