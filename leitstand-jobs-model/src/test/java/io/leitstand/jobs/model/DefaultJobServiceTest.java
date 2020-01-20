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
import static io.leitstand.jobs.service.ReasonCode.JOB0100E_JOB_NOT_FOUND;
import static io.leitstand.jobs.service.ReasonCode.JOB0109E_CANNOT_COMMIT_COMPLETED_JOB;
import static io.leitstand.security.auth.UserName.userName;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.ConflictException;
import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.db.DatabaseService;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.Repository;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobSubmission;
import io.leitstand.security.auth.UserName;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJobServiceTest {
	
	@Mock
	private Repository repository;
	
	@Mock
	private InventoryClient inventory;
	
	@Mock
	private DatabaseService db;
	
	@Mock
	private JobEditor editor;
	
	@Mock
	private Messages messages;
	
	@Mock
	private UserName userName = userName("unittest");
	
	@InjectMocks
	private DefaultJobService service = new DefaultJobService();
	
	@Test
	public void store_job_creates_new_job_if_no_job_with_specified_id_exists() {
		JobId id = randomJobId();
		JobSubmission submission = mock(JobSubmission.class);
		ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		doNothing().when(editor).updateJob(jobCaptor.capture(),eq(submission));
		service.storeJob(id,submission);
		assertEquals(id,jobCaptor.getValue().getJobId());
		
	}
	
	@Test
	public void cannot_commit_job_that_does_not_exist() {
		try {
			service.commitJob(randomJobId());
			fail("EntityNotFoundException expected");
		} catch (EntityNotFoundException e) {
			assertThat(e.getReason(),is(JOB0100E_JOB_NOT_FOUND));
		}
	}

	@Test
	public void cannot_commit_completed_job() {
		Job job = mock(Job.class);
		when(repository.execute(any(Query.class))).thenReturn(job);
		when(job.isCompleted()).thenReturn(Boolean.TRUE);
		
		try {
			service.commitJob(randomJobId());
			fail("ConflictException expected");
		} catch (ConflictException e) {
			assertThat(e.getReason(),is(JOB0109E_CANNOT_COMMIT_COMPLETED_JOB));
		}
	}
	
	@Test
	public void run_job_immediately_when_no_schedule_date_is_set() {
		Job job = mock(Job.class);
		when(repository.execute(any(Query.class))).thenReturn(job);
		ArgumentCaptor<Date> dateScheduled = ArgumentCaptor.forClass(Date.class);
		doNothing().when(job).setDateScheduled(dateScheduled.capture());
		service.commitJob(randomJobId());
		
		verify(job).setDateScheduled(dateScheduled.getValue());
		verify(job).submit();
	}
	
	@Test
	public void store_creates_new_job_when_job_not_exists() {
		ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		doNothing().when(repository).add(jobCaptor.capture());
		
		service.storeJob(randomJobId(),mock(JobSubmission.class));
		Job job = jobCaptor.getValue();
		assertNotNull(job);
		verify(repository).add(job);
		
	}

	
}
