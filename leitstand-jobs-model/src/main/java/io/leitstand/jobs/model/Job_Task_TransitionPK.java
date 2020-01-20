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
import java.util.Objects;


public class Job_Task_TransitionPK implements Serializable, Comparable<Job_Task_TransitionPK> {

	private static final long serialVersionUID = 1L;

	private Long from;
	private Long to;
	
	public Job_Task_TransitionPK(){
		// Ctor
	}
	
	public Job_Task_TransitionPK(Long from, Long to){
		this.from = from;
		this.to = to;
	}
	
	public Long getFrom(){
		return from;
	}
	
	public Long getTo(){
		return to;
	}
	
	@Override
	public int compareTo(Job_Task_TransitionPK o) {
		int fromOrder = from.compareTo(o.from);
		if(fromOrder != 0){
			return fromOrder;
		}
		return to.compareTo(o.to);
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(from,to);
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
		Job_Task_TransitionPK pk = (Job_Task_TransitionPK) o;
		if(from.longValue() != pk.from.longValue()){
			return false;
		}
		if(to.longValue() != pk.to.longValue()){
			return false;
		}
		return true;
	}
	
}
