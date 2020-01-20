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

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.leitstand.inventory.service.ElementGroupId;
import io.leitstand.inventory.service.ElementGroupName;
import io.leitstand.inventory.service.ElementGroupSettings;
import io.leitstand.inventory.service.ElementGroupSettingsService;
import io.leitstand.inventory.service.ElementGroupType;
import io.leitstand.inventory.service.ElementId;
import io.leitstand.inventory.service.ElementName;
import io.leitstand.inventory.service.ElementSettings;
import io.leitstand.inventory.service.ElementSettingsService;

@RequestScoped
public class InventoryClient {

	private ElementGroupSettingsService groups;

	private ElementSettingsService elements;
	
	
	private Map<ElementGroupId,ElementGroupSettings> groupCache;
	private Map<ElementId,ElementSettings> elementCache;
	
	protected InventoryClient() {
		// Make InventoryClient proxyable.
	}

	@Inject
	public InventoryClient(ElementGroupSettingsService groups, ElementSettingsService elements) {
		this.groups = groups;
		this.elements = elements;
	}

	@PostConstruct
	protected void initCaches() {
		groupCache = new HashMap<>();
		elementCache = new HashMap<>();
	}
	
	public ElementGroupSettings getGroupSettings(Job job) {
		if(job == null) {
			return null;
		}
		return getGroupSettings(job.getGroupId());
	}
	
	public ElementGroupSettings getGroupSettings(ElementGroupId id) {
		if(id == null) {
			return null;
		}
		if(groupCache.containsKey(id)) {
			return groupCache.get(id);
		}
		
		ElementGroupSettings settings = groups.getGroupSettings(id);
		groupCache.put(id,settings);
		return settings;
		
	}
	
	public ElementSettings getElementSettings(Job_Task task) {
		if(task == null) {
			return null;
		}
		return getElementSettings(task.getElementId());
	}
	
	public ElementSettings getElementSettings(ElementId id) {
		if(id == null) {
			return null;
		}
		if(elementCache.containsKey(id)) {
			return elementCache.get(id);
		}
		
		ElementSettings settings = elements.getElementSettings(id);
		elementCache.put(id,settings);
		return settings;
	}

	public ElementSettings getElementSettings(ElementName name) {
		for(ElementSettings settings : elementCache.values()) {
			if(settings == null) {
				continue;
			}
			if(settings.getElementName().equals(name)) {
				return settings;
			}
		}
		
		ElementSettings settings = elements.getElementSettings(name);
		if(settings != null) {
			elementCache.put(settings.getElementId(),settings);
		}
		return settings;
		
	}

	public ElementGroupSettings getGroupSettings(ElementGroupType groupType,
												 ElementGroupName groupName) {
		for(ElementGroupSettings settings : groupCache.values()) {
			if(settings == null) {
				continue;
			}
			if(settings.getGroupName().equals(groupName) && settings.getGroupType().equals(groupType)) {
				return settings;
			}
		}
		
		ElementGroupSettings settings = groups.getGroupSettings(groupType,
															    groupName);
		if(settings != null) {
			groupCache.put(settings.getGroupId(),
						   settings);
		}
		return settings;
	}

	public Map<ElementId,ElementSettings> getElements(Job job){
		for(Job_Task task : job.getTasks().values()) {
			ElementId elementId = task.getElementId();
			if(elementId != null) {
				// Load element settings into cache.
				getElementSettings(elementId);
			}
		}
		return unmodifiableMap(elementCache);
	}
	
}
