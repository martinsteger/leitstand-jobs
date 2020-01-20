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

import java.util.HashSet;
import java.util.Set;

public class JobGraph  {

	private Job flow;
	private Set<Job_Task_Transition> edges;
	private Set<Job_Task> nodes;
	
	public JobGraph(Job flow){
		this.flow = flow;
		this.edges = new HashSet<>();
		this.nodes = new HashSet<>();
	}
	
	public void accept(JobGraphVisitor visitor){
		Job_Task start = flow.getStart();
		traverse(visitor,start);
	}
	
	private void traverse(JobGraphVisitor visitor, Job_Task task){
		if(nodes.add(task)) {
			visitor.visitNode(task);
		}
		for(Job_Task_Transition transition : task.getSuccessors()){
			traverse(visitor,transition.getTo());
			if(edges.add(transition)){
				visitor.visitEdge(transition);
			}
		}
	}
}
