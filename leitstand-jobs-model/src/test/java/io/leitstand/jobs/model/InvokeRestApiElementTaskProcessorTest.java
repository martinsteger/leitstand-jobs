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
package io.leitstand.jobs.model;

import static io.leitstand.jobs.service.JobId.randomJobId;
import static io.leitstand.jobs.service.JobName.jobName;
import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static io.leitstand.jobs.service.TaskState.ACTIVE;
import static io.leitstand.jobs.service.TaskState.FAILED;
import static io.leitstand.jobs.service.TaskState.REJECTED;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;

import io.leitstand.jobs.service.JobName;
import io.leitstand.jobs.service.TaskState;

public class InvokeRestApiElementTaskProcessorTest {

	static JsonObject asJson(String message) {
		try(JsonReader reader = Json.createReader(new StringReader(message))) {
			return reader.readObject();
		}
	}
	
	
	private InvokeElementRestApiTaskProcessor processor;
	private InventoryClient inventory;
	private Job_Task task;
	private Job job;
	
	
	@Before
	public void initTestResources() {
		inventory = mock(InventoryClient.class);
		// FIXME Get rid of that spy!!
		processor = new InvokeElementRestApiTaskProcessor(inventory);
		job = mock(Job.class);
		when(job.getJobId()).thenReturn(randomJobId());
		when(job.getJobName()).thenReturn(jobName("unit-job_name"));
		task = mock(Job_Task.class);
		when(task.getJob()).thenReturn(job);
		when(task.getTaskId()).thenReturn(randomTaskId());
	}

	

	
	@Test
	public void mark_task_as_active_if_accepted_by_device() {
		Response response = mock(Response.class);
		doReturn(Status.ACCEPTED.getStatusCode()).when(response).getStatus();
		TaskState outcome = processor.mapStatusCodeToTaskState(response);
		assertThat(outcome, is(ACTIVE));
	}
	
	@Test
	public void mark_task_as_rejected_if_rejected_by_device() {
		Response response = mock(Response.class);
		doReturn(Status.CONFLICT.getStatusCode()).when(response).getStatus();
		TaskState outcome = processor.mapStatusCodeToTaskState(response);
		assertThat(outcome, is(REJECTED));
	}

	@Test
	public void mark_task_as_failed_if_device_response_is_unknown() {
		Response response = mock(Response.class);
		doReturn(Status.INTERNAL_SERVER_ERROR.getStatusCode()).when(response).getStatus();
		TaskState outcome = processor.mapStatusCodeToTaskState(response);
		assertThat(outcome, is(FAILED));
	}
	
	
}


