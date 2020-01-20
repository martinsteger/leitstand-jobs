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
package io.leitstand.jobs.jpa;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.UUID;

import javax.persistence.AttributeConverter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.leitstand.commons.model.Scalar;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.TaskId;

@RunWith(Parameterized.class)
public class StringScalarConverterTest {

	@Parameters
	public static Collection<Object[]> converters(){
		String uuid = UUID.randomUUID().toString();
		Object[][] converters = new Object[][]{
			{new JobIdConverter(),  	uuid	,	new JobId(uuid)},
			{new TaskIdConverter(),	uuid,	new TaskId(uuid)},
		};
		return asList(converters);
	}
	
	private AttributeConverter<Scalar<String>,String> converter;
	private Scalar<String> scalar;
	private String string;
	
	public StringScalarConverterTest(AttributeConverter<Scalar<String>,String> converter,
									 String string,
									 Scalar<String> scalar) {
		this.converter = converter;
		this.string = string;
		this.scalar = scalar;
		
	}
	
	@Test
	public void empty_string_is_mapped_to_null() throws Exception {
		assertNull(converter.convertToEntityAttribute(""));
	}
	
	@Test
	public void null_string_is_mapped_to_null() throws Exception {
		assertNull(converter.convertToEntityAttribute(null));
	}
	
	@Test
	public void valid_string_is_adaptFromJsonled_properly() throws Exception{
		assertEquals(scalar,converter.convertToEntityAttribute(string));
	}
	
	@Test
	public void non_null_scalar_is_adaptToJsonled_properly() throws Exception {
		assertEquals(string,converter.convertToDatabaseColumn(scalar));
	}
	
	@Test
	public void null_scalar_is_mapped_to_null() throws Exception{
		assertNull(converter.convertToDatabaseColumn(null));
	}
	
}
