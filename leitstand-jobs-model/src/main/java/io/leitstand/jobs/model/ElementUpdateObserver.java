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

import static io.leitstand.inventory.service.OperationalState.MAINTENANCE;
import static io.leitstand.inventory.service.OperationalState.OPERATIONAL;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.leitstand.inventory.service.ElementService;
import io.leitstand.jobs.service.TaskType;
//TODO Move to update app
//TODO Check for application name
@Dependent
public class ElementUpdateObserver {

	@Inject
	private ElementService inventory;
	
	public void taskUpdate(@Observes TaskStateChangedEvent event){
		Job_Task task = event.getTask();
		if(task.isElementTask()) {
			TaskType type = task.getTaskType();
			if("activate".equals(type.toString())){ // TODO Introduce enum of available tasks!
				if(task.isActive()){
					inventory.updateElementOperationalState(task.getElementId(), MAINTENANCE);
				} else {
					inventory.updateElementOperationalState(task.getElementId(), OPERATIONAL);
				}
			}
		
		}
		
	}
}
