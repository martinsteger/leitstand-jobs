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

import static io.leitstand.commons.model.StringUtil.isNonEmptyString;
import static java.lang.String.format;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.leitstand.inventory.service.ElementId;
import io.leitstand.inventory.service.ElementSettings;
import io.leitstand.jobs.service.TaskId;

public class JobExport implements JobGraphVisitor{
	
	private static final String BG_COLOR_ACTIVE     = "#2d5c9b"; 
	private static final String BG_COLOR_TIMEDOUT   = "#62248a"; 
	private static final String BG_COLOR_FAILED     = "#e82000"; 
	private static final String BG_COLOR_COMPLETED  = "#308720"; 
	private static final String BG_COLOR_CANCELLED  = "#e5800d"; 
	private static final String BG_COLOR_DEFAULT	= "#A0A0A0";
	
	private static final String FONT_COLOR_LIGHT	= "white";
	private static final String FONT_COLOR_DARK	   	= "black";
	
	
	private List<String> nodes = new LinkedList<>();
	private List<String> edges = new LinkedList<>();
	private Map<TaskId, String> nodeNames = new HashMap<>();
	private Map<ElementId,ElementSettings> elements;

	private String fontName = "Arial";
	private int fontSize = 11;

	public JobExport(Map<ElementId,ElementSettings> elements) {
		this.elements = elements;
	}
	
	@Override
	public void visitNode(Job_Task node) {
		String id = "T" + nodes.size();
		nodeNames.put(node.getTaskId(), id);
		if(node.getElementId() != null) {
			nodes.add(format("%s [id=\"%s\" shape=\"%s\" fontname=\"%s\" fontsize=\"%d\" fontweight=\"bold\" fontcolor=\"%s\" label=\"%s\" style=\"filled\" color=\"%s\" tooltip=\"%s\"]",
					id,
					node.getTaskId(),
					"box",
					fontName,
					Integer.valueOf(fontSize),
					fontcolor(node),
					name(node),
					color(node),
					tooltip(node)));
			return;
		} 
		nodes.add(format("%s [id=\"%s\" shape=\"%s\" style=\"rounded,filled\" color=\"%s\" height=0.08 width=2.0 fixedsize=true label=\"\" tooltip=\"Barrier to wait for previous tasks before starting next task group\"]",
				  id,
				  node.getTaskId(),
				  "box",
				  color(node)));
	}

	private String name(Job_Task node){
		ElementSettings element = elements.get(node.getElementId());
		if(element == null) {
			return format("%s\n%s\n%s",
						  node.getTaskType(),
						  node.getTaskName(),
						  node.getTaskState());
		}

		return format("%s\n%s\n%s\n%s\n%s",
				  	  node.getTaskType(),
				  	  element.getElementRole(),
				  	  element.getElementName(),
				  	  element.getElementAlias(),
				  	  node.getTaskState());

		
	}
	
	
	private String tooltip(Job_Task node){
		ElementSettings element = elements.get(node.getElementId());
		if(element!=null && isNonEmptyString(element.getDescription())) {
			return element.getDescription();
		}
		return "Task "+node.getTaskId();
		
	}
	

	private static String color(Job_Task node){
		if(node.isSucceeded() || node.isSuspended()){
			return BG_COLOR_COMPLETED;
		}
		if(node.isTimedOut()) {
			return BG_COLOR_TIMEDOUT;
		}
		if(node.isCancelled() || node.isSkipped()) {
			return BG_COLOR_CANCELLED;
		}
		if(node.isFailed()){
			return BG_COLOR_FAILED;
		}
		if(node.isActive()){
			return BG_COLOR_ACTIVE;
		}
		return BG_COLOR_DEFAULT;
	}
	
	private static String fontcolor(Job_Task node){
		if(node.isBlocked()){
			return FONT_COLOR_LIGHT;
		}
		if(node.isSuspended()) {
			return FONT_COLOR_LIGHT;
		}
		if(node.isSucceeded()){
			return FONT_COLOR_LIGHT;
		}
		if(node.isFailed()){
			return FONT_COLOR_LIGHT;
		}
		if(node.isActive()){
			return FONT_COLOR_LIGHT;
		}
		if(node.isTimedOut()) {
			return FONT_COLOR_LIGHT;
		}
		return FONT_COLOR_DARK;
	}
	
	@Override
	public void visitEdge(Job_Task_Transition transition) {
		TaskId from = transition.getFrom().getTaskId();
		TaskId to = transition.getTo().getTaskId();
		String fromId = nodeNames.get(from);
		String toId = nodeNames.get(to);
		String style = transition.getFrom().isCanary() ? "dashed"  : "solid";
		edges.add(String.format("%s -> %s [penwidth=\"%d\" color=\"#202020\"  style=\"%s\" tooltip=\"%s\" arrowhead=open arrowsize=0.75]",
								fromId,
								toId,
								1,
								style,
								"Next step"));
	}

	public String getDot() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("digraph g {")
			  .append("\n")
			  .append("ordering=out")
			  .append("\n")
			  .append("tooltip=\"Job task execution flow \"")
			  .append("\n")
			  .append("splines=curves")
			  .append("\n");
		for (String node : nodes) {
			buffer.append(node).append(";\n");
		}
		buffer.append("\n");
		for (String edge : edges) {
			buffer.append(edge).append(";\n");
		}
		buffer.append("}");

		return buffer.toString();
	}

}
