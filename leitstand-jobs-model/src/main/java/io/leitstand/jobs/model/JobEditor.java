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

import static io.leitstand.commons.model.ObjectUtil.asSet;
import static io.leitstand.jobs.model.Job.findAllTransitions;
import static io.leitstand.jobs.model.Job_Task.findSuccessorsOfCompletedTasks;
import static io.leitstand.jobs.model.Job_Task.setTaskStateToReadyForExecution;
import static java.util.Objects.hash;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.leitstand.commons.model.Repository;
import io.leitstand.jobs.service.JobSubmission;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskSubmission;
import io.leitstand.jobs.service.TaskTransitionSubmission;
@Dependent
public class JobEditor {

	/** A helper class to index all submitted transitions.*/
	private static final class SubmittedTransition {
		
		private TaskId from;
		private TaskId to;
		SubmittedTransition(TaskId from, TaskId to){
			this.from = from;
			this.to = to;
		}
		
		@Override
		public int hashCode(){
			return hash(from,to);
		}
		
		@Override
		public boolean equals(Object o){
			if(o == null){
				return false;
			}
			if(o == this){
				return true;
			}
			if(o.getClass() != getClass()){
				return false;
			}
			SubmittedTransition transition = (SubmittedTransition) o;
			return Objects.equals(from, transition.from) && 
				   Objects.equals(to, transition.to);
		}
		
	}
	
	
	private Repository repository;
	
	@Inject
	public JobEditor(@Jobs Repository repository){
		this.repository = repository;
	}
	
	public Set<Job_Task> loadTasksToProceedWithFor(Job job){
		Set<Job_Task> tasks = new LinkedHashSet<>();
		for(Job_Task task : repository.execute(findSuccessorsOfCompletedTasks(job))){
			if(task.isBlocked()){
				continue;
			}
			tasks.add(task);
		}
		if(tasks.isEmpty()) {
			Job_Task start = job.getStart();
			if(start.isResumable()) {
				return asSet(start);
			}
		}
		return tasks;
	}
	
	public void prepareTaskFlowForExecution(Job job){
		job.submit();
		repository.execute(setTaskStateToReadyForExecution(job));
	}
	
	public void updateJob(Job job, JobSubmission submission){
		if(job.isSubmitted()){
			throw new IllegalStateException("Flow already submitted!"); // TODO Improve exception
		}
		updateTasks(job, submission);
		updateTransitions(job, submission);
	}
	
	private void updateTasks(Job job, JobSubmission submission) {
		Map<TaskId,TaskSubmission> submittedTasks = new HashMap<>();
		// Index all submitted tasks.
		for(TaskSubmission task : submission.getTasks()){
			submittedTasks.put(task.getTaskId(), task);
		}
		// Update all existing tasks.
		// Search for orphaned tasks.
		List<Job_Task> orphanedTasks = new LinkedList<>();
		for(Job_Task task : job.getTasks().values()){
			TaskSubmission taskSubmission = submittedTasks.remove(task.getTaskId());
			if(taskSubmission == null){
				// Task is not longer part of submitted task and orphaned.
				orphanedTasks.add(task);
				continue;
			}
			// Update the parameters of the existing task.
			task.setParameter(task.getParameters());
		}

		// Add all new tasks.
		// All remaining tasks in the submitted tasks are new, 
		// because they did not match to existing tasks.
		for(TaskSubmission taskSubmission : submittedTasks.values()){
			Job_Task task = new Job_Task(job,
										 taskSubmission.getTaskType(),
										 taskSubmission.getTaskId(),
										 taskSubmission.getTaskName(),
										 taskSubmission.getElementId(),
										 taskSubmission.getParameter());
			job.addTask(task);
		}
		
		// Remove all orphaned tasks.
		for(Job_Task orphan : orphanedTasks){
			job.removeTask(orphan);
			repository.remove(orphan);
		}
		
		// Set the job start task.
		job.setStart(job.getTask(submission.getStartTaskId()));
		
	}

	private void updateTransitions(Job job, JobSubmission submission) {
		Map<SubmittedTransition,TaskTransitionSubmission> submittedTransitions = new HashMap<>();
		// Index all submitted transitions.
		for(TaskTransitionSubmission transition : submission.getTransitions()){
			submittedTransitions.put(new SubmittedTransition(transition.getFrom(),
													transition.getTo()),
											  		transition);
		}
		// Update all existing transitions.
		// Search all orphaned transitions, i.e. transitions that are not needed anymore
		List<Job_Task_Transition> orphanTransitions = new LinkedList<>();
		for(Job_Task_Transition transition : repository.execute(findAllTransitions(job))){
			TaskTransitionSubmission transitionSubmission = submittedTransitions.remove(new SubmittedTransition(transition.getFrom().getTaskId(),
																									   transition.getTo().getTaskId()));
			if(transitionSubmission == null){
				// Submitted transition does not contain the current transition.
				// Add it to orphaned transitions and continue with next transitions.
				orphanTransitions.add(transition);
				continue;
			}
			// Transition still exist. Remaining action is to update the transition name.
			transition.setName(transitionSubmission.getName());
		}
		
		Map<TaskId,Job_Task> tasks = job.getTasks();
		// Remove all orphaned transactions.
		for(Job_Task_Transition orphan : orphanTransitions){
			Job_Task from = orphan.getFrom();
			from.removeSuccessor(orphan.getTo());
		}
		// Add all remaining submitted transitions.
		// This transitions are new transitions, because there was not match for them.
		for(TaskTransitionSubmission transitionSubmission : submittedTransitions.values() ){
			Job_Task from = tasks.get(transitionSubmission.getFrom());
			Job_Task to = tasks.get(transitionSubmission.getTo());
			from.addSuccessor(to,transitionSubmission.getName());
		}
	}
	
}
