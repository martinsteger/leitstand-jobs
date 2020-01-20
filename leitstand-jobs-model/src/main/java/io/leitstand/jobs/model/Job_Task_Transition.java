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

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(schema="job", name="job_task_transition")
@IdClass(Job_Task_TransitionPK.class)
public class Job_Task_Transition implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@ManyToOne
	@JoinColumn(name="from_task_id", nullable=false)
	private Job_Task from;
	
	@Id
	@ManyToOne
	@JoinColumn(name="to_task_id", nullable=false)
	private Job_Task to;
	
	private String name;
	
	protected Job_Task_Transition(){
		//JPA
	}
	
	public Job_Task_Transition(Job_Task from, Job_Task to){
		this.from = from;
		this.to = to;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Job_Task getFrom() {
		return from;
	}
	
	public Job_Task getTo() {
		return to;
	}
	
}
