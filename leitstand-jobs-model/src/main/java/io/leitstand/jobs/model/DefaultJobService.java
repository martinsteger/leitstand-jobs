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

import static io.leitstand.commons.db.DatabaseService.prepare;
import static io.leitstand.commons.jpa.BooleanConverter.parseBoolean;
import static io.leitstand.commons.messages.MessageFactory.createMessage;
import static io.leitstand.commons.model.ObjectUtil.not;
import static io.leitstand.commons.model.ObjectUtil.optional;
import static io.leitstand.jobs.model.Job.findByJobId;
import static io.leitstand.jobs.service.JobApplication.jobApplication;
import static io.leitstand.jobs.service.JobFlow.newJobFlow;
import static io.leitstand.jobs.service.JobId.jobId;
import static io.leitstand.jobs.service.JobInfo.newJobInfo;
import static io.leitstand.jobs.service.JobName.jobName;
import static io.leitstand.jobs.service.JobProgress.newJobProgress;
import static io.leitstand.jobs.service.JobSchedule.newJobSchedule;
import static io.leitstand.jobs.service.JobSettings.newJobSettings;
import static io.leitstand.jobs.service.JobSubmission.newJobSubmission;
import static io.leitstand.jobs.service.JobTask.newJobTask;
import static io.leitstand.jobs.service.JobTasks.newJobTasks;
import static io.leitstand.jobs.service.JobType.jobType;
import static io.leitstand.jobs.service.ReasonCode.JOB0100E_JOB_NOT_FOUND;
import static io.leitstand.jobs.service.ReasonCode.JOB0101I_JOB_SETTINGS_UPDATED;
import static io.leitstand.jobs.service.ReasonCode.JOB0102E_JOB_SETTINGS_IMMUTABLE;
import static io.leitstand.jobs.service.ReasonCode.JOB0103I_JOB_CONFIRMED;
import static io.leitstand.jobs.service.ReasonCode.JOB0104I_JOB_CANCELLED;
import static io.leitstand.jobs.service.ReasonCode.JOB0105I_JOB_RESUMED;
import static io.leitstand.jobs.service.ReasonCode.JOB0107I_JOB_STORED;
import static io.leitstand.jobs.service.ReasonCode.JOB0109E_CANNOT_COMMIT_COMPLETED_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0110E_CANNOT_RESUME_COMPLETED_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0111E_JOB_NOT_REMOVABLE;
import static io.leitstand.jobs.service.ReasonCode.JOB0112I_JOB_REMOVED;
import static io.leitstand.jobs.service.TaskState.ACTIVE;
import static io.leitstand.jobs.service.TaskState.CANCELLED;
import static io.leitstand.jobs.service.TaskState.COMPLETED;
import static io.leitstand.jobs.service.TaskState.CONFIRM;
import static io.leitstand.jobs.service.TaskState.FAILED;
import static io.leitstand.jobs.service.TaskState.READY;
import static io.leitstand.jobs.service.TaskState.REJECTED;
import static io.leitstand.jobs.service.TaskState.TIMEOUT;
import static io.leitstand.jobs.service.TaskState.taskState;
import static io.leitstand.jobs.service.TaskSubmission.newTaskSubmission;
import static io.leitstand.jobs.service.TaskTransitionSubmission.newTaskTransitionSubmission;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.persistence.LockModeType.NONE;
import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.LockModeType;

import io.leitstand.commons.ConflictException;
import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.db.DatabaseService;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Repository;
import io.leitstand.commons.model.Service;
import io.leitstand.inventory.service.ElementGroupId;
import io.leitstand.inventory.service.ElementGroupSettings;
import io.leitstand.inventory.service.ElementId;
import io.leitstand.inventory.service.ElementSettings;
import io.leitstand.jobs.service.JobFlow;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobInfo;
import io.leitstand.jobs.service.JobProgress;
import io.leitstand.jobs.service.JobQuery;
import io.leitstand.jobs.service.JobService;
import io.leitstand.jobs.service.JobSettings;
import io.leitstand.jobs.service.JobSubmission;
import io.leitstand.jobs.service.JobTask;
import io.leitstand.jobs.service.JobTasks;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskState;
import io.leitstand.jobs.service.TaskSubmission;
import io.leitstand.jobs.service.TaskTransitionSubmission;
import io.leitstand.security.auth.Authenticated;
import io.leitstand.security.auth.UserName;
@Service
public class DefaultJobService implements JobService {
	
	private static final Logger LOG = Logger.getLogger(DefaultJobService.class.getName());
	
	static class TaskByStateCount {
		
		private int[] counts = new int[TaskState.values().length];
		
		void increment(TaskState state) {
			counts[state.ordinal()]++;
		}
		
		int getCount(TaskState state) {
			return counts[state.ordinal()];
		}
		
	}

	@Inject
	@Jobs
	private Repository repository;
	
	@Inject
	private InventoryClient inventory;
	
	@Inject
	@Jobs
	private DatabaseService db;
	
	@Inject
	private JobEditor editor;
	
	@Inject
	private Messages messages;

	@Inject
	@Authenticated
	private UserName user;
	
	protected DefaultJobService() {
		
	}
	
	DefaultJobService(Repository repository,
					  DatabaseService db,
					  InventoryClient inventory,
					  JobEditor jobEditor,
					  Messages messages,
					  UserName user){
		this.repository = repository;
		this.db = db;
		this.inventory = inventory;
		this.editor = jobEditor;
		this.messages = messages;
		this.user = user;
	}
	
	@Override
	public JobSubmission getJobSubmission(JobId jobId) {
		Job job = findJob(jobId,NONE);
		List<TaskSubmission> tasks = new LinkedList<>();
		List<TaskTransitionSubmission> transitions = new LinkedList<>();
		for(Job_Task task : job.getTasks().values()){
			ElementSettings element = inventory.getElementSettings(task);
			tasks.add(newTaskSubmission()
					  .withElementId(optional(element,ElementSettings::getElementId))
					  .withElementName(optional(element,ElementSettings::getElementName))
					  .withTaskId(task.getTaskId())
					  .withTaskName(task.getTaskName())
					  .withTaskType(task.getTaskType())
					  .withCanary(task.isCanary())
					  .withParameter(task.getParameters()) 
					  .build());
			
			for(Job_Task_Transition transition : task.getSuccessors()){
				transitions.add(newTaskTransitionSubmission()
							    .from(task.getTaskId())
								.to(transition.getTo().getTaskId())
								.withName(transition.getName())
								.build());
			}
			
		}

		
		ElementGroupId groupId = job.getGroupId();
		if(groupId != null) {
			ElementGroupSettings group = inventory.getGroupSettings(groupId);
			return newJobSubmission()
				   .withGroupId(group.getGroupId())
				   .withGroupName(group.getGroupName())
				   .withJobApplication(job.getJobApplication())
				   .withJobType(job.getJobType())
				   .withJobName(job.getJobName())
				   .withTasks(tasks)
				   .withTransitions(transitions)
				   .build();
		}
		return newJobSubmission()
			   .withJobApplication(job.getJobApplication())
			   .withJobType(job.getJobType())
			   .withJobName(job.getJobName())
			   .withTasks(tasks)
			   .withTransitions(transitions)
			   .build();

	}

	public JobProgress getJobProgress(JobId jobId) {
		Job job = findJob(jobId,NONE);
		Set<Job_Task> elements = new HashSet<>();
		job.getTasks();
		TaskByStateCount stats = new TaskByStateCount();
		traverse(elements, job.getStart(),stats);
		
		return newJobProgress()
			   .withActiveCount(stats.getCount(ACTIVE))
			   .withReadyCount(stats.getCount(READY)+stats.getCount(CONFIRM))
			   .withCompletedCount(stats.getCount(COMPLETED))
			   .withFailedCount(stats.getCount(REJECTED)+stats.getCount(FAILED))
			   .withTimeoutCount(stats.getCount(TIMEOUT))
			   .build();
				
	}
	
	private void traverse(Set<Job_Task> tasks, Job_Task task, TaskByStateCount stats){
		if(tasks.add(task)) {
			stats.increment(task.getTaskState());
			for(Job_Task_Transition transition : task.getSuccessors()){
				traverse(tasks, transition.getTo(),stats);
			}
		}
	}
	
	@Override
	public JobFlow getJobFlow(JobId jobId) {
		Job job = findJob(jobId,NONE);
		ElementGroupSettings group = inventory.getGroupSettings(job);
		Map<ElementId,ElementSettings> elements = inventory.getElements(job);
		JobExport export = new JobExport(elements);
		JobGraph  graph = new JobGraph(job);
		graph.accept(export);
		
		return newJobFlow()
			   .withGroupId(optional(group, ElementGroupSettings::getGroupId))
			   .withGroupName(optional(group,ElementGroupSettings::getGroupName))
			   .withJobApplication(job.getJobApplication())
			   .withJobId(job.getJobId())
			   .withJobName(job.getJobName())
			   .withJobOwner(job.getJobOwner())
			   .withJobState(job.getJobState())
			   .withJobType(job.getJobType())
			   .withGraph(export.getDot())
			   .build();
	
	}
	
	@Override
	public void storeJob(JobId jobId, JobSubmission submission) {
		Job job = repository.execute(findByJobId(jobId));
		if(job == null){
			job = new Job(submission.getJobApplication(),
						  submission.getJobType(),
						  jobId,
						  submission.getJobName(),
						  user); 
			job.setGroupId(submission.getGroupId());
			repository.add(job);
			LOG.fine(() -> format("%s: Job %s (%s) stored. Owner: %s", 
								  JOB0107I_JOB_STORED.getReasonCode(),
								  submission.getJobName(),
								  jobId,
								  user));
		} 
		editor.updateJob(job, submission);
		messages.add(createMessage(JOB0107I_JOB_STORED, 
								   submission.getJobName(), 
								   job.getJobId()));
	}

	
	@Override
	public void commitJob(JobId jobId) {
		Job job = findJob(jobId,PESSIMISTIC_WRITE);
		if(job.isCompleted()) {
			LOG.fine(() -> format("%s: Job %s (%s) is already completed (%s) and cannot be committed again. Owner: ",
								  JOB0109E_CANNOT_COMMIT_COMPLETED_JOB.getReasonCode(),
								  job.getJobName(),
								  job.getJobId(),
								  job.getJobState(),
								  job.getJobOwner()));
			throw new ConflictException(JOB0109E_CANNOT_COMMIT_COMPLETED_JOB, jobId);
		}
		if(job.getDateScheduled() == null) {
			job.setDateScheduled(new Date());
		}
		LOG.fine(() -> format("%s: Job %s (%s) stored. Owner: %s", 
							  JOB0107I_JOB_STORED.getReasonCode(),
							  job.getJobName(),
							  job.getJobId(),
							  job.getJobOwner()));
		messages.add(createMessage(JOB0107I_JOB_STORED, 
					   			   job.getJobName(), 
					   			   job.getJobId()));

		editor.prepareTaskFlowForExecution(job);
		job.submit();
	}
	
	@Override
	public JobSettings getJobSettings(JobId jobId) {
		Job job = findJob(jobId,NONE);
		return newJobSettings()
			   .withJobId(job.getJobId())
			   .withJobName(job.getJobName())
			   .withJobApplication(job.getJobApplication())
			   .withJobType(job.getJobType())
			   .withJobState(job.getJobState())
			   .withSchedule(newJobSchedule()
					   		 .withAutoResume(job.isAutoResume())
					   		 .withStartTime(job.getDateScheduled())
					   		 .withEndTime(job.getDateSuspend())
							 .build())
			   .withDateModified(job.getDateModified())
			   .build();
	}


	@Override
	public JobTasks getJobTasks(JobId jobId) {
		Job job = findJob(jobId,NONE);
		
		List<JobTask> tasks = job.getOrderedTasks()
								 .stream()
								 .filter(task -> task.getElementId() != null || task.getParameters() != null)
								 .map(task -> taskInfo(task))
								 .collect(toList());
		
		ElementGroupSettings group = inventory.getGroupSettings(job);
			
		return newJobTasks()
			   .withGroupId(optional(group, ElementGroupSettings::getGroupId))
			   .withGroupName(optional(group, ElementGroupSettings::getGroupName))
			   .withJobId(job.getJobId())
			   .withJobApplication(job.getJobApplication())
			   .withJobName(job.getJobName())
			   .withJobState(job.getJobState())
			   .withJobType(job.getJobType())
			   .withJobOwner(job.getJobOwner())
			   .withTasks(tasks)
			   .build();
		
	}
	
	
	@Override
	public JobInfo getJobInfo(JobId jobId) {
		
		Job job = findJob(jobId,NONE);
		
		List<JobTask> tasks = job.getOrderedTasks()
								 .stream()
								 .filter(task -> task.getElementId() != null || task.getParameters() != null)
								 .map(task -> taskInfo(task))
								 .collect(toList());
		
		TaskByStateCount stats = new TaskByStateCount();
		Set<Job_Task> elements = new LinkedHashSet<>();
		traverse(elements, job.getStart(),stats);
		
			
		JobProgress progress = newJobProgress()
							   .withActiveCount(stats.getCount(ACTIVE))
							   .withReadyCount(stats.getCount(READY))
							   .withCompletedCount(stats.getCount(COMPLETED)+stats.getCount(CONFIRM))
							   .withFailedCount(stats.getCount(REJECTED)+stats.getCount(FAILED))
							   .withTimeoutCount(stats.getCount(TIMEOUT))
							   .build();
				
		ElementGroupSettings group = inventory.getGroupSettings(job.getGroupId());
		return newJobInfo()
			   .withJobId(job.getJobId())
			   .withJobApplication(job.getJobApplication())
			   .withJobType(job.getJobType())
			   .withJobName(job.getJobName())
			   .withJobOwner(job.getJobOwner())
			   .withJobState(job.getJobState())
			   .withGroupId(optional(group, ElementGroupSettings::getGroupId))
			   .withGroupName(optional(group, ElementGroupSettings::getGroupName))
			   .withTasks(tasks)
			   .withProgress(progress)
			   .withSchedule(newJobSchedule()
					   		 .withStartTime(job.getDateScheduled())
					   		 .withEndTime(job.getDateSuspend())
					   		 .withAutoResume(job.isAutoResume()))
			   .build();
	}


	private JobTask taskInfo(Job_Task task) {
		ElementSettings element = inventory.getElementSettings(task);
		return newJobTask()
			   .withTaskId(task.getTaskId())
			   .withTaskName(task.getTaskName())
			   .withTaskType(task.getTaskType())
			   .withTaskState(task.getTaskState())
			   .withElementId(optional(element, ElementSettings::getElementId))
			   .withElementName(optional(element, ElementSettings::getElementName))
			   .withElementAlias(optional(element,ElementSettings::getElementAlias))
			   .withElementRole(optional(element,ElementSettings::getElementRole))
			   .withGroupId(optional(element,ElementSettings::getGroupId))
			   .withGroupName(optional(element,ElementSettings::getGroupName))
			   .withGroupType(optional(element,ElementSettings::getGroupType))
			   .withParameter(task.getParameters())
			   .withDateLastModified(task.getDateModified())
			   .build();
	}
	


	private Job findJob(JobId jobId, LockModeType lockMode) {
		Job job = repository.execute(findByJobId(jobId,lockMode));
		if(job == null){
			LOG.fine(() -> format("%s: Job %s does not exist", 
								  JOB0100E_JOB_NOT_FOUND.getReasonCode(),
								  jobId));
			throw new EntityNotFoundException(JOB0100E_JOB_NOT_FOUND,jobId);
		}
		return job;
	}

	@Override
	public List<TaskId> resumeJob(JobId jobId) {
		Job job = findJob(jobId,PESSIMISTIC_WRITE);
		if(job.isCompleted()) {
			LOG.fine(()-> format("%s: Cannot resume completed job %s (%s). Job State: %s, Owner: %s",
								 JOB0110E_CANNOT_RESUME_COMPLETED_JOB.getReasonCode(),
								 job.getJobName(),
								 job.getJobId(),
								 job.getJobState(),
								 job.getJobOwner()));
			messages.add(createMessage(JOB0110E_CANNOT_RESUME_COMPLETED_JOB,
									   job.getJobName(),
									   job.getJobId()));	
			return emptyList();
		}
		if(job.isFailed() || job.isCancelled()) {
			for(Job_Task task : job.getTaskList()) {
				if(task.isResumable()) {
					task.setTaskState(READY);
				}
			}
			List<TaskId> tasks = editor.loadTasksToProceedWithFor(job)
		 	 	  		 			   .stream()
		 	 	  		 			   .map(Job_Task::getTaskId)
		 	 	  		 			   .collect(toList());
			
			if(!tasks.isEmpty()) {
				job.setJobState(ACTIVE);
			}
			return tasks;
			
		}

		LOG.fine(()-> format("%s: Resumed job %s (%s). Job State: %s, Owner: %s",
							 JOB0105I_JOB_RESUMED.getReasonCode(),
							 job.getJobName(),
							 job.getJobId(),
							 job.getJobState(),
							 job.getJobOwner()));
		
		messages.add(createMessage(JOB0105I_JOB_RESUMED, 
								   job.getJobName(),
								   job.getJobId()));
		
		return editor.loadTasksToProceedWithFor(job)
			 	 	 .stream()
			 	 	 .map(Job_Task::getTaskId)
			 	 	 .collect(toList());
		
	}
	
	
	@Override
	public List<JobSettings> findJobs(JobQuery query) {
		
		String sql = "SELECT j.application, j.type, j.uuid,j.name, j.state, j.tsmodified, j.autoresume, j.tsschedule, j.tssuspend "+
				 	 "FROM job.job j "+
				 	 "WHERE (j.name ~ ? "+
				 	 "OR j.application ~ ?"+
				 	 "OR j.type ~ ? ) ";
		List<Object> args = new LinkedList<>();
		args.add(query.getFilter());
		args.add(query.getFilter());
		args.add(query.getFilter());
		
		if(query.isRunningOnly()) {
			  sql+= "AND j.state IN ('ACTIVE','CONFIRM','TIMEDOUT') ";
		}
		if(query.getScheduledAfter() != null) {
			  sql += "AND j.tsscheduled > ? ";
			  args.add(query.getScheduledAfter());
		}
		if(query.getScheduledBefore() != null) {
			sql += "AND j.tsscheduled < ? ";
			args.add(query.getScheduledBefore());
		}
		
		sql+= "ORDER BY j.tsschedule DESC, j.name, j.type, j.application ";
		
		
		return db.executeQuery(prepare(sql,args), 
							  rs -> newJobSettings()
							  		.withJobApplication(jobApplication(rs.getString(1)))
							  		.withJobType(jobType(rs.getString(2)))
							  		.withJobId(jobId(rs.getString(3)))
							  		.withJobName(jobName(rs.getString(4)))
									.withJobState(taskState(rs.getString(5)))
									.withDateModified(rs.getTimestamp(6))
									.withSchedule(newJobSchedule()
												  .withAutoResume(parseBoolean(rs.getString(7)))
												  .withStartTime(rs.getTimestamp(8))
												  .withEndTime(rs.getTimestamp(9))
												  .build())
									.build());
	}

	@Override
	public void updateJobState(JobId jobId, TaskState state) {
		Job job = repository.execute(findByJobId(jobId));
		job.setJobState(state);	
	}

	@Override
	public void cancelJob(JobId jobId) {
		Job job = findJob(jobId,PESSIMISTIC_WRITE);
		if(job.isCompleted()) {
			messages.add(createMessage(JOB0104I_JOB_CANCELLED, 
					   				   job.getJobId(), 
					   				   job.getJobName(), 
					   				   job.getJobApplication()));
			return;
		}
		messages.add(createMessage(JOB0104I_JOB_CANCELLED, 
								   job.getJobId(), 
								   job.getJobName(), 
								   job.getJobApplication()));
		job.setJobState(CANCELLED);
		job.getTasks()
		   .values()
		   .stream()
		   .filter(not(Job_Task::isTerminated))
		   .forEach(task -> task.setTaskState(CANCELLED));
	}

	@Override
	public List<TaskId> confirmJob(JobId jobId) {
		Job job = findJob(jobId,PESSIMISTIC_WRITE);
		if(job.isSuspended()) {
			job.setJobState(ACTIVE);
			LOG.fine(()->format("%s: Job %s (%s) confirmed.",
								JOB0103I_JOB_CONFIRMED.getReasonCode(),
								job.getJobName(),
								job.getJobId()));
			messages.add(createMessage(JOB0103I_JOB_CONFIRMED, 
					   				   job.getJobId(), 
					   				   job.getJobName(), 
					   				   job.getJobApplication()));
			return job.getTasks()
					  .values()
					  .stream()
					  .filter(Job_Task::isSuspended)
					  .map(Job_Task::getTaskId)
					  .collect(toList());
		}
		return emptyList();	
	}

	@Override
	public void storeJobSettings(JobId jobId, JobSettings settings) {
		Job job = findJob(jobId,PESSIMISTIC_WRITE);
		if(job.isCompleted() || job.isRunning()) {
			LOG.fine(()->format("%s: Job %s (%s) settings must not be modified.",
								JOB0102E_JOB_SETTINGS_IMMUTABLE.getReasonCode(),
								job.getJobName(),
								job.getJobId()));
			throw new ConflictException(JOB0102E_JOB_SETTINGS_IMMUTABLE, 
										jobId, 
										job.getJobName(),
										job.getJobApplication());
		}
		job.setJobName(settings.getJobName());
		job.setDateScheduled(settings.getSchedule().getDateScheduled());
		job.setDateSuspend(settings.getSchedule().getDateSuspend());
		job.setAutoResume(settings.getSchedule().isAutoResume());
		LOG.fine(()->format("%s: Job %s (%s) settings updated.",
							JOB0101I_JOB_SETTINGS_UPDATED.getReasonCode(),
							job.getJobName(),
							job.getJobId()));
		messages.add(createMessage(JOB0101I_JOB_SETTINGS_UPDATED, 
								   job.getJobId(), 
								   job.getJobName(), 
								   job.getJobApplication()));
	}

	@Override
	public void removeJob(JobId jobId) {
		Job job = findJob(jobId, PESSIMISTIC_WRITE);
		if(job.isTerminated()) {
			LOG.fine(()->format("%s: Job %s (%s) removed.",
								JOB0112I_JOB_REMOVED.getReasonCode(),
								job.getJobName(),
								job.getJobId()));
			messages.add(createMessage(JOB0112I_JOB_REMOVED, 
									   job.getJobId(),
									   job.getJobName(),
									   job.getJobApplication()));
			repository.remove(job);
			return;
		}
		LOG.fine(()->format("%s: Job %s (%s) cannot be removed. State: %s.",
							JOB0111E_JOB_NOT_REMOVABLE.getReasonCode(),
							job.getJobName(),
							job.getJobId(),
							job.getJobState()));
		throw new ConflictException(JOB0111E_JOB_NOT_REMOVABLE, 
									job.getJobId(),
									job.getJobName(),
									job.getJobApplication());
	}
}
