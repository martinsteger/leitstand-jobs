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

import static io.leitstand.commons.json.JsonMarshaller.marshal;
import static io.leitstand.commons.json.JsonUnmarshaller.unmarshal;
import static io.leitstand.commons.json.SerializableJsonObject.serializable;
import static io.leitstand.commons.json.SerializableJsonObject.unwrap;
import static io.leitstand.jobs.service.State.ACTIVE;
import static io.leitstand.jobs.service.State.CANCELLED;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.State.CONFIRM;
import static io.leitstand.jobs.service.State.FAILED;
import static io.leitstand.jobs.service.State.NEW;
import static io.leitstand.jobs.service.State.READY;
import static io.leitstand.jobs.service.State.REJECTED;
import static io.leitstand.jobs.service.State.SKIPPED;
import static io.leitstand.jobs.service.State.TIMEOUT;
import static io.leitstand.jobs.service.State.WAITING;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.leitstand.commons.jpa.BooleanConverter;
import io.leitstand.commons.jpa.SerializableJsonObjectConverter;
import io.leitstand.commons.json.SerializableJsonObject;
import io.leitstand.commons.model.AbstractEntity;
import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.Update;
import io.leitstand.commons.model.ValueObject;
import io.leitstand.inventory.jpa.ElementIdConverter;
import io.leitstand.inventory.service.ElementId;
import io.leitstand.jobs.jpa.TaskIdConverter;
import io.leitstand.jobs.jpa.TaskNameConverter;
import io.leitstand.jobs.jpa.TaskTypeConverter;
import io.leitstand.jobs.service.JobApplication;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobName;
import io.leitstand.jobs.service.JobType;
import io.leitstand.jobs.service.State;
import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskName;
import io.leitstand.jobs.service.TaskType;
import io.leitstand.security.auth.UserName;

@Entity
@Table(schema="job", name="job_task")
@NamedQuery(name="Job_Task.findByTaskId", 
			query="SELECT t FROM Job_Task t WHERE t.taskId=:id")
@NamedQuery(name="Job_Task.setTasksWaitingForExecution", 
			query="UPDATE Job_Task t "+
				  "SET t.taskState=io.leitstand.jobs.service.State.WAITING "+
				  "WHERE t.job=:job")
@NamedQuery(name="Job_Task.markExpiredTasks",
			query="UPDATE Job_Task t "+
				  "SET t.taskState=io.leitstand.jobs.service.State.TIMEOUT "+
				  "WHERE t.taskState=io.leitstand.jobs.service.State.ACTIVE "+
				  "AND t.dateModified < :expired")
public class Job_Task extends AbstractEntity{

	private static final long serialVersionUID = 1L;

	public static Query<Job_Task> findTaskById(TaskId id) {
		return em -> em.createNamedQuery("Job_Task.findByTaskId", Job_Task.class)
				       .setParameter("id",id)
				       .getSingleResult();
	}
	
	public static Update markExpiredTasks(Date expiryDate){
		return em -> em.createNamedQuery("Job_Task.markExpiredTasks",Job_Task.class)
					   .setParameter("expired",expiryDate)
					   .executeUpdate();
	}

	public static Update setTaskStateToWaitingForExecution(Job job) {
		return em -> em.createNamedQuery("Job_Task.setTasksWaitingForExecution")
					   .setParameter("job", job)
					   .executeUpdate();
	}

	// A task will be scheduled only if the predecessor is completed and all
	// other tasks with a higher priority are completed as well
	
	@OneToMany(mappedBy="from", cascade=ALL, orphanRemoval=true)
	private List<Job_Task_Transition> successors;
	
	@ManyToOne
	@JoinColumn(name="job_id")
	private Job job;

	@Column(name="uuid", unique=true)
	@Convert(converter=TaskIdConverter.class)
	private TaskId taskId;
	
	@Column(name="type")
	@Convert(converter=TaskTypeConverter.class)
	private TaskType taskType;
	
	@Column(name="name")
	@Convert(converter=TaskNameConverter.class)
	private TaskName taskName;
	
	@Enumerated(STRING)
	@Column(name="state")
	private State taskState;

	@Column(name="element_uuid")
	@Convert(converter=ElementIdConverter.class)
	private ElementId elementId;

	@Convert(converter=BooleanConverter.class)
	private boolean suspend;
	
	@Column(name="parameter")
	@Convert(converter=SerializableJsonObjectConverter.class)
	private SerializableJsonObject parameter;
	
	@OneToMany(mappedBy="to", cascade=ALL, orphanRemoval=true)
	private List<Job_Task_Transition> predecessors;
	
	protected Job_Task(){
		//JPA
		this.successors = new LinkedList<>();
		this.predecessors = new LinkedList<>();
	}
	
	public Job_Task(Job job, 
					TaskType taskType, 
					TaskId taskId, 
					TaskName taskName){
		this(job,
		     taskType,
		     taskId,
		     taskName,
		     null,
		     null);
	}
	
	protected Job_Task(Long id,
	                   Job job, 
                       TaskType taskType, 
                       TaskId taskId, 
                       TaskName taskName){
   this(id,
        job,
        taskType,
        taskId,
        taskName,
        null,
        null);
}
	
	public Job_Task(Job job, 
					TaskType taskType, 
					TaskId taskId, 
					TaskName taskName, 
					ElementId elementId){
		this(job,
		     taskType,
		     taskId,
		     taskName,
		     elementId,
		     null);
	}
	
    protected Job_Task(Long id,
                       Job job, 
                       TaskType taskType, 
                       TaskId taskId, 
                       TaskName taskName, 
                       ElementId elementId){
        this(id,
             job,
             taskType,
             taskId,
             taskName,
             elementId,
             null);
    }
	
	public Job_Task(Job job, 
					TaskType taskType, 
					TaskId taskId, 
					TaskName taskName, 
					JsonObject parameter){
		this(job,
		     taskType,
		     taskId,
		     taskName,
		     null,
		     parameter);
	}
	
	
	public Job_Task(Job job, 
					TaskType taskType, 
					TaskId taskId, 
					TaskName taskName, 
					ElementId elementId, 
					JsonObject parameter){
		this.taskId = taskId;
		this.taskType = taskType;
		this.taskName = taskName;
		this.successors = new LinkedList<>();
		this.predecessors = new LinkedList<>();
		this.taskState = NEW;
		this.elementId = elementId;
		this.parameter = serializable(parameter);
		this.job = job;
		job.addTask(this);
	}
	
	public Job_Task(Long id,
	                Job job, 
                    TaskType taskType, 
                    TaskId taskId, 
                    TaskName taskName, 
                    ElementId elementId, 
                    JsonObject parameter){
	   super(id);
       this.taskId = taskId;
       this.taskType = taskType;
       this.taskName = taskName;
       this.successors = new LinkedList<>();
       this.predecessors = new LinkedList<>();
       this.taskState = NEW;
       this.elementId = elementId;
       this.parameter = serializable(parameter);
       this.job = job;
       job.addTask(this);
	}
	
	public JobId getJobId() {
		return job.getJobId();
	}
	
	public UserName getJobOwner() {
		return job.getJobOwner();
	}
	
	public JobName getJobName() {
		return job.getJobName();
	}
	
	public void setTaskState(State state) {
		this.taskState = state;
	}
	
	public TaskId getTaskId() {
		return taskId;
	}
	
	public State getTaskState() {
		return taskState;
	}
	
	public boolean isTerminated(){
		return getTaskState().isTerminalState();
	}
	
	public boolean isFailed(){
		return getTaskState() == FAILED;
	}

	public boolean isWaiting() {
	    return getTaskState() == WAITING;
	}
	
	public boolean isSucceeded(){
		return getTaskState() == COMPLETED;
	}
	
	public boolean isActive(){
		return getTaskState() == ACTIVE;
	}
	
	public boolean isReady(){
		return getTaskState() == READY;
	}
	
	public void addSuccessor(Job_Task task) {
		addSuccessor(task,null);
	}
	

	public void addSuccessor(Job_Task task, String name) {
		Job_Task_Transition transition = findTransitionToSuccessor(task);
		if(transition != null){
			transition.setName(name);
			return;
		}
		transition = new Job_Task_Transition(this,task);
		transition.setName(name);
		successors.add(transition);
		task.onAddPredecessor(this);
	}

	private Job_Task_Transition findTransitionToPredecessor(Job_Task task){
		for(Job_Task_Transition transition : predecessors){
			if(transition.getFrom().equals(task)){
				return transition;
			}
		}
		return null;
	}

	protected void onAddPredecessor(Job_Task task) {
		Job_Task_Transition transition = findTransitionToPredecessor(task);
		if(transition == null){
			predecessors.add(task.findTransitionToSuccessor(this));
		}
	}
	
	protected void onRemovePredecessor(Job_Task task) {
		Job_Task_Transition transition = findTransitionToPredecessor(task);
		if(transition != null){
			predecessors.remove(transition);
		}
	}
	
	public void removeSuccessor(Job_Task task){
		Job_Task_Transition transition = findTransitionToSuccessor(task);
		if(transition != null){
			task.onRemovePredecessor(this);
			successors.remove(transition);
		}
	}
	
	public boolean isSuccessorOf(Job_Task task){
		return findTransitionToPredecessor(task) != null;
	}

	
	public boolean isPredecessorOf(Job_Task task){
		return findTransitionToSuccessor(task) != null;
	}

	protected Job_Task_Transition findTransitionToSuccessor(Job_Task task) {
		for(Job_Task_Transition transition : successors){
			if(transition.getTo().equals(task)){
				return transition;
			}
		}
		return null;
	}

	@Override
	public String toString(){
		return format("%s@(%d)",getClass().getSimpleName(),getId());
	}

	public void setParameter(JsonObject parameter) {
		this.parameter = serializable(parameter);
	}

	public void setParameter(ValueObject object) {
		setParameter(marshal(object));
	}
	
	public JsonObject getParameters() {
		return unwrap(parameter);
	}
	
	public <T> T getParameters(Class<T> type) {
		JsonObject parameters = getParameters();
		return unmarshal(type, parameters);
	}
	
	public ElementId getElementId(){
		return elementId;
	}
	
	public List<Job_Task_Transition> getSuccessors() {
	    List<Job_Task_Transition> orderedSuccessors = new LinkedList<>(successors);
	    orderedSuccessors.sort((a,b) -> Long.compare(a.getTo().getId(),b.getTo().getId()));
		return unmodifiableList(orderedSuccessors);
	}

	public List<Job_Task_Transition> getPredecessors() {
        return unmodifiableList(predecessors);
    }

	public boolean isEligibleForExecution() {
	    if(isReady()) {
	        return true;
	    }
	    if(isWaiting()) {
	        for(Job_Task_Transition t : getPredecessors()) {
	            Job_Task predecessor = t.getFrom();
	            if(predecessor.isSucceeded()) {
	                continue;
	            }
	            return false;
	        }
	        return true;
	    }
	    return false;
	}
	
	public void setCanary(boolean suspend) {
		this.suspend = suspend;
	}
	
	public boolean isCanary(){
		return suspend;
	}

	public Job getJob() {
		return job;
	}

	public boolean isSuspended() {
		return getTaskState() == CONFIRM;
	}

	public boolean isRejected() {
		return getTaskState() == REJECTED;
	}

	public boolean isTimedOut() {
		return getTaskState() == TIMEOUT;
	}
	
	public boolean isSkipped() {
		return getTaskState() == SKIPPED;
	}

	public TaskName getTaskName() {
		return taskName;
	}
	
	public TaskType getTaskType() {
		return taskType;
	}

	public boolean isElementTask() {
		return elementId != null;
	}
	
	public void join(Job_Task... tasks) {
		for(Job_Task task : tasks){
			task.addSuccessor(this);
		}
	}
	
	public boolean isForkTask() {
		return successors.size() > 1;
	}

	public void setParameter(Map<String, Object> params) {
		setParameter(marshal(params));
	}

	public boolean isCancelled() {
		return getTaskState() == CANCELLED;
	}
	
	public boolean isNew() {
	    return getTaskState() == NEW;
	}

	public boolean isResumable() {
		if(isSucceeded()) {
			// A succeeded task does not have to be executed again.
			return false;
		}
		if(isActive()) {
			// An active task does not have to be started again.
			return false;
		}
		if(isNew()) {
		    // A new task is not eligible for execution and cannot be executed.
		    return false;
		}
		return true;
	}

	public JobType getJobType() {
		return job.getJobType();
	}
	
	public JobApplication getJobApplication() {
		return job.getJobApplication();
	}

  
	
}
