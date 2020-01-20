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
import static io.leitstand.jobs.service.JobSubmission.newJobSubmission;
import static io.leitstand.jobs.service.TaskSubmission.newTaskSubmission;
import static io.leitstand.jobs.service.TaskTransitionSubmission.newTaskTransitionSubmission;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.leitstand.commons.model.Repository;
import io.leitstand.jobs.service.JobSubmission;
import io.leitstand.jobs.service.TaskId;

public class ParallelizeSerialFlowIT extends JobsIT{

	
	private JobEditor service;
	private Job flow;
	private long start;
	
	@Before
	public void setup_serial_sequence(){
		this.service = new JobEditor(new Repository(getEntityManager()));
		this.flow = new Job(null,null,randomJobId(), null, null);
		Job_Task first  = new Job_Task(flow,null, new TaskId("PF_TASK_1"), null,null,null);
		Job_Task second = new Job_Task(flow,null, new TaskId("PF_TASK_2"), null,null,null);
		Job_Task third  = new Job_Task(flow,null, new TaskId("PF_TASK_3"), null,null,null);
		first.addSuccessor(second);
		second.addSuccessor(third);
		flow.addTask(first);
		flow.addTask(second);
		flow.addTask(third);
		flow.setStart(first);
		getEntityManager().persist(flow);
		this.start = System.currentTimeMillis();
	}
	
	@Test
	public void apply_transformation_to_parallel_flow(){
		transaction(()->{
			flow = getEntityManager().find(Job.class, flow.getId());
			Map<TaskId,Job_Task> tasks = flow.getTasks();
			Job_Task first = tasks.get(new TaskId("PF_TASK_1"));
			Job_Task second = tasks.get(new TaskId("PF_TASK_2"));
			Job_Task third = tasks.get(new TaskId("PF_TASK_3"));
			assertThat(first, instanceOf(Job_Task.class));
			assertThat(second, instanceOf(Job_Task.class));
			assertThat(third, instanceOf(Job_Task.class));
			assertTrue(first.isPredecessorOf(second));
			assertTrue(second.isPredecessorOf(third));
			
			
			JobSubmission submission = newJobSubmission().withTasks(newTaskSubmission().withTaskId(new TaskId("PF_FORK")),
																			  newTaskSubmission().withTaskId(new TaskId("PF_TASK_1")),
																			  newTaskSubmission().withTaskId(new TaskId("PF_TASK_2")),
																			  newTaskSubmission().withTaskId(new TaskId("PF_TASK_3")),
																			  newTaskSubmission().withTaskId(new TaskId("PF_JOIN")))
																   .withTransitions(newTaskTransitionSubmission().from(new TaskId("PF_FORK")).to(new TaskId("PF_TASK_1")).withName("F->1"),
																		   			newTaskTransitionSubmission().from(new TaskId("PF_FORK")).to(new TaskId("PF_TASK_2")).withName("F->2"),
																		   			newTaskTransitionSubmission().from(new TaskId("PF_FORK")).to(new TaskId("PF_TASK_3")).withName("F->3"),
																		   			newTaskTransitionSubmission().from(new TaskId("PF_TASK_1")).to(new TaskId("PF_JOIN")).withName("1->J"),
																		   			newTaskTransitionSubmission().from(new TaskId("PF_TASK_2")).to(new TaskId("PF_JOIN")).withName("2->J"),
																		   			newTaskTransitionSubmission().from(new TaskId("PF_TASK_3")).to(new TaskId("PF_JOIN")).withName("3->J"))
																   .build();
			
			service.updateJob(flow, submission);
		});
		
	}
	
	@After
	public void verify_parallel_sequence(){
		transaction(()->{
			System.out.println("Updated flow. That took: "+(System.currentTimeMillis()-start)+" millis");
			flow = getEntityManager().find(Job.class, flow.getId());
			Map<TaskId,Job_Task> tasks = flow.getTasks();
			Job_Task fork = tasks.get(new TaskId("PF_FORK"));
			Job_Task first = tasks.get(new TaskId("PF_TASK_1"));
			Job_Task second = tasks.get(new TaskId("PF_TASK_2"));
			Job_Task third = tasks.get(new TaskId("PF_TASK_3"));
			Job_Task join = tasks.get(new TaskId("PF_JOIN"));
			
			assertTrue(fork.isPredecessorOf(first));
			assertTrue(fork.isPredecessorOf(second));
			assertTrue(fork.isPredecessorOf(third));
			assertTrue(first.isPredecessorOf(join));
			assertTrue(second.isPredecessorOf(join));
			assertTrue(third.isPredecessorOf(join));
			assertFalse(join.isPredecessorOf(first));
			assertFalse(join.isPredecessorOf(second));
			assertFalse(join.isPredecessorOf(third));
			assertFalse(first.isPredecessorOf(fork));
			assertFalse(second.isPredecessorOf(fork));
			assertFalse(third.isPredecessorOf(fork));
			assertFalse(first.isPredecessorOf(second));
			assertFalse(second.isPredecessorOf(third));
		});
		
	}
}
