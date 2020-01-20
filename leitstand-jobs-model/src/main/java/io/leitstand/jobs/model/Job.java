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

import static io.leitstand.jobs.service.TaskState.ACTIVE;
import static io.leitstand.jobs.service.TaskState.CANCELLED;
import static io.leitstand.jobs.service.TaskState.COMPLETED;
import static io.leitstand.jobs.service.TaskState.CONFIRM;
import static io.leitstand.jobs.service.TaskState.FAILED;
import static io.leitstand.jobs.service.TaskState.NEW;
import static io.leitstand.jobs.service.TaskState.READY;
import static io.leitstand.jobs.service.TaskState.SKIPPED;
import static io.leitstand.jobs.service.TaskState.TIMEOUT;
import static java.util.Collections.unmodifiableMap;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.LockModeType.OPTIMISTIC_FORCE_INCREMENT;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.LockModeType;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

import io.leitstand.commons.jpa.BooleanConverter;
import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.VersionableEntity;
import io.leitstand.inventory.jpa.ElementGroupIdConverter;
import io.leitstand.inventory.service.ElementGroupId;
import io.leitstand.jobs.jpa.JobApplicationConverter;
import io.leitstand.jobs.jpa.JobNameConverter;
import io.leitstand.jobs.jpa.JobTypeConverter;
import io.leitstand.jobs.service.JobApplication;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobName;
import io.leitstand.jobs.service.JobType;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskState;
import io.leitstand.security.auth.UserName;
import io.leitstand.security.auth.jpa.UserNameConverter;

@Entity
@Table(schema="job", name="job")
@NamedQueries({
@NamedQuery(name="Job.loadAllTransitionsAndTasks",
			query="SELECT j FROM Job_Task_Transition j WHERE j.to.job=:job"),
@NamedQuery(name="Job.findByJobId",
			query="SELECT j FROM Job j WHERE j.uuid=:id"),
@NamedQuery(name="Job.findByTaskId",
			query="SELECT j FROM Job j INNER JOIN j.tasks t WHERE t.taskId=:id "),

@NamedQuery(name="Job.findReadyAndActiveByElementGroup",
			query="SELECT j FROM Job j "+
				  "WHERE j.groupId=:groupId "+
				  "AND (j.state=io.leitstand.jobs.service.TaskState.READY "+
				       "OR j.state=io.leitstand.jobs.service.TaskState.ACTIVE)" ),
@NamedQuery(name="Job.findJobs",
			query="SELECT j FROM Job j ORDER BY j.tsschedule DESC"),
@NamedQuery(name="Job.findRunnableJobs",
			query="SELECT j FROM Job j "+
				  "WHERE j.state=io.leitstand.jobs.service.TaskState.READY "+
				  "AND j.tsschedule < :scheduled" )
})
public class Job extends VersionableEntity {
	
	private static final long serialVersionUID = 1L;

	public static Query<List<Job>> findJobs() {
		return em -> em.createNamedQuery("Job.findJobs",Job.class)
					   .setMaxResults(100)
					   .getResultList();
	}
	
	public static Query<List<Job>> findRunnableJobs(Date scheduled){
		return em -> em.createNamedQuery("Job.findRunnableJobs", Job.class)
					   .setParameter("scheduled", scheduled, TIMESTAMP)
					   .setMaxResults(50)
					   .getResultList();
	}
	
	public static Query<List<Job_Task_Transition>> findAllTransitions(Job job){
		return em -> em.createNamedQuery("Job.loadAllTransitionsAndTasks",Job_Task_Transition.class)
					   .setParameter("job",job)
					   .getResultList();
	}
	
	public static Query<Job> findByJobId(JobId id) {
		return em -> em.createNamedQuery("Job.findByJobId",Job.class)
					   .setParameter("id", id.toString())
					   .getSingleResult();
	}
	
	public static Query<Job> findByJobId(JobId id, LockModeType lockMode) {
		return em -> em.createNamedQuery("Job.findByJobId",Job.class)
					   .setParameter("id", id.toString())
					   .setLockMode(lockMode)
					   .getSingleResult();
	}
	
	public static Query<Job> findJobByTaskId(TaskId taskId) {
		return findJobByTaskId(taskId,OPTIMISTIC_FORCE_INCREMENT);
	}
	
	public static Query<Job> findJobByTaskId(TaskId taskId, LockModeType lockMode) {
		return em -> em.createNamedQuery("Job.findByTaskId",Job.class)
					   .setParameter("id",taskId)
					   .setLockMode(lockMode)
					   .getSingleResult();
	}
	
	public static Query<List<Job>> findReadyAndActiveJobsForElementGroup(ElementGroupId groupId) {
		return em -> em.createNamedQuery("Job.findReadyAndActiveByElementGroup",Job.class)
				   	   .setParameter("groupId", groupId)
				   	   .getResultList();
		
	}
	
	@Column(name="elementgroup_uuid")
	@Convert(converter=ElementGroupIdConverter.class)
	private ElementGroupId  groupId;
	
	@OneToMany(mappedBy="job",cascade=ALL)
	@MapKey(name="taskId")
	private Map<TaskId,Job_Task> tasks;
	
	@Temporal(TIMESTAMP)
	private Date tsschedule;
	
	@Temporal(TIMESTAMP)
	private Date tssuspend;
	
	@Convert(converter=BooleanConverter.class)
	private boolean autoResume;

	@OneToOne(cascade=ALL)
	@JoinColumn(name="start_task_id")
	private Job_Task start;
	
	@Enumerated(STRING)
	private TaskState state;
	
	@Column
	@Convert(converter=JobNameConverter.class)
	private JobName name;

	@Column
	@Convert(converter=JobTypeConverter.class)
	private JobType type;
	
	@Column
	@Convert(converter=JobApplicationConverter.class)
	private JobApplication application;

	@Convert(converter=UserNameConverter.class)
	private UserName owner;
	
	protected Job(){
		
	}
	
	public Job(JobApplication application, 
			   JobType type, 
			   JobId jobId, 
			   JobName name,
			   UserName owner){
		super(jobId.toString());
		this.application = application;
		this.owner = owner;
		this.type = type;
		this.name = name;
		this.tasks = new HashMap<>();
		this.state = NEW;
	}
	
	void addTask(Job_Task task){
		this.tasks.put(task.getTaskId(),task);
	}
	
	public void setGroupId(ElementGroupId groupId) {
		this.groupId = groupId;
	}
	
	public ElementGroupId getGroupId() {
		return groupId;
	}
	
	public Job_Task getStart() {
		return start;
	}
	
	public void setStart(Job_Task start) {
		this.start = start;
	}
	
	public void setDateScheduled(Date tsschedule) {
		this.tsschedule = tsschedule;
	}
	
	public void setDateSuspend(Date tssuspend){
		this.tssuspend = tssuspend;
	}
	
	public void setAutoResume(boolean auto){
		this.autoResume = auto;
	}
	
	public Date getDateScheduled() {
		return tsschedule;
	}
	
	public Date getDateSuspend() {
		return tssuspend;
	}
	
	public boolean isAutoResume() {
		return autoResume;
	}
	
	public boolean isSubmitted() {
		return state == READY;
	}
	
	public void submit(){
		if(start == null){
			throw new IllegalStateException("Cannot submit job without start node being specified!");
		}
		tasks.values().forEach(task -> task.setTaskState(READY));
		if(NEW == state){
			state = READY;
		}
		// In any other case the task job was already submitted an no further action is required
	}
	
	public JobId getJobId() {
		return new JobId(getUuid());
	}

	public Map<TaskId,Job_Task> getTasks() {
		return unmodifiableMap(new HashMap<>(tasks));
	}

	public void removeTask(Job_Task task) {
		tasks.remove(task.getTaskId());
	}
	
	public void setJobName(JobName name) {
		this.name = name;
	}
	
	public JobType getJobType() {
		return type;
	}
	
	public JobApplication getJobApplication() {
		return application;
	}
	
	public void setJobOwner(UserName owner) {
		this.owner = owner;
	}
	
	public UserName getJobOwner() {
		return owner;
	}

	public TaskState getJobState() {
		return state;
	}

	public void setJobState(TaskState state) {
		this.state = state;
	}

	public void failed() {
		this.state = FAILED;
		this.tasks.values()
				  .stream()
				  .filter(Job_Task::isReady)
				  .forEach(task -> task.setTaskState(SKIPPED));
	}
	
	public boolean isInState(TaskState... states){
		for(TaskState s : states){
			if(this.state == s){
				return true;
			}
		}
		return false;
	}

	public boolean isNotInState(TaskState...states) {
		return !isInState(states);
	}

	public boolean isTerminated() {
		return state.isTerminalState();
	}

	public boolean isRunning() {
		return state == ACTIVE;
	}

	public boolean isFailed() {
		return state == FAILED;
	}
	
	public boolean isCompleted(){
		return state == COMPLETED;
	}
	
	public boolean isCancelled(){
		return state == CANCELLED;
	}
	
	public boolean isSuspended(){
		return state == CONFIRM;
	}

	public boolean isTimedOut() {
		return state == TIMEOUT;
	}

	public boolean completed() {
		for(Job_Task task : getTasks().values()) {
			if(task.isSucceeded()) {
				continue;
			}
			return false;
		}
		this.state = COMPLETED;
		return true;
	}

	public Job_Task getTask(TaskId taskId) {
		return getTasks().get(taskId);
	}
	
	public Set<Job_Task> getOrderedTasks(){
		Set<Job_Task> tasks = new LinkedHashSet<>();
		tasks.add(getStart());
		traverse(tasks, getStart());
		return tasks;
	}
	
	private void traverse(Set<Job_Task> tasks, Job_Task task) {
		for(Job_Task_Transition transition : task.getSuccessors()) {
			tasks.add(transition.getTo());
		}
		for(Job_Task_Transition transition : task.getSuccessors()) {
			traverse(tasks, transition.getTo());
		}
	}

	public JobName getJobName() {
		return name;
	}

	public List<Job_Task> getTaskList() {
		return new ArrayList<>(getTasks().values());
	}
	
}
