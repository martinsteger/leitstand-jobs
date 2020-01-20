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

import static io.leitstand.inventory.service.ElementGroupId.randomGroupId;
import static io.leitstand.inventory.service.ElementId.randomElementId;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import io.leitstand.inventory.service.ElementGroupId;
import io.leitstand.inventory.service.ElementGroupName;
import io.leitstand.inventory.service.ElementGroupSettings;
import io.leitstand.inventory.service.ElementGroupSettingsService;
import io.leitstand.inventory.service.ElementGroupType;
import io.leitstand.inventory.service.ElementId;
import io.leitstand.inventory.service.ElementName;
import io.leitstand.inventory.service.ElementSettings;
import io.leitstand.inventory.service.ElementSettingsService;

public class InventoryClientTest {

	private ElementGroupSettingsService groups;
	private ElementSettingsService elements;
	private InventoryClient client;
	
	private ElementId elementId;
	private ElementName elementName;
	private ElementSettings element;
	
	private ElementGroupId groupId;
	private ElementGroupType groupType;
	private ElementGroupName groupName;
	private ElementGroupSettings group;
	
	@Before
	public void initClient() {
		groups = mock(ElementGroupSettingsService.class);
		elements = mock(ElementSettingsService.class);
		client = new InventoryClient(groups,elements);
		client.initCaches();
		
		elementId = randomElementId();
		elementName = new ElementName("unit-test-element");
		element = mock(ElementSettings.class);
		when(element.getElementId()).thenReturn(elementId);
		when(element.getElementName()).thenReturn(elementName);

		groupId = randomGroupId();
		groupName = new ElementGroupName("unit-test-group");
		groupType = new ElementGroupType("unit");
		group = mock(ElementGroupSettings.class);
		when(group.getGroupId()).thenReturn(groupId);
		when(group.getGroupName()).thenReturn(groupName);
		when(group.getGroupType()).thenReturn(groupType);
		
		when(elements.getElementSettings(elementId)).thenReturn(element);
		when(elements.getElementSettings(elementName)).thenReturn(element);
		when(groups.getGroupSettings(groupId)).thenReturn(group);
		when(groups.getGroupSettings(groupType,groupName)).thenReturn(group);
		
	}
	
	@Test
	public void return_null_if_group_id_is_null() {
		assertNull(client.getGroupSettings((ElementGroupId)null));
	}
	
	@Test
	public void return_null_if_group_name_is_null() {
		assertNull(client.getGroupSettings(groupType,null));
	}
	
	@Test
	public void return_null_if_job_is_null() {
		assertNull(client.getGroupSettings((Job)null));
	}
	
	
	@Test
	public void return_null_if_element_id_is_null() {
		assertNull(client.getElementSettings((ElementId)null));
	}
	
	@Test
	public void return_null_if_element_name_is_null() {
		assertNull(client.getElementSettings((ElementName)null));
	}
	
	@Test
	public void return_null_if_task_is_null() {
		assertNull(client.getElementSettings((Job_Task)null));
	}
	
	@Test
	public void cache_element_by_id() {
		ElementSettings a = client.getElementSettings(elementId);
		ElementSettings b = client.getElementSettings(elementId);
		assertSame(a, b);
		verify(elements).getElementSettings(elementId);
	}
	
	@Test
	public void cache_element_by_name() {
		ElementSettings a = client.getElementSettings(elementName);
		ElementSettings b = client.getElementSettings(elementName);
		assertSame(a, b);
		verify(elements).getElementSettings(elementName);
	}
	
	@Test
	public void find_cached_element_by_name() {
		ElementSettings a = client.getElementSettings(elementId);
		ElementSettings b = client.getElementSettings(elementName);
		assertSame(a, b);
		verify(elements).getElementSettings(elementId);
		verifyNoMoreInteractions(elements);

	}
	
	
	@Test
	public void cache_group_by_id() {
		ElementGroupSettings a = client.getGroupSettings(groupId);
		ElementGroupSettings b = client.getGroupSettings(groupId);
		assertSame(a, b);
		verify(groups).getGroupSettings(groupId);
	}
	
	@Test
	public void cache_group_by_name() {
		ElementGroupSettings a = client.getGroupSettings(groupType, groupName);
		ElementGroupSettings b = client.getGroupSettings(groupType, groupName);
		assertSame(a, b);
		verify(groups).getGroupSettings(groupType, groupName);
	}
	
	@Test
	public void find_cached_group_by_name() {
		ElementGroupSettings a = client.getGroupSettings(groupId);
		ElementGroupSettings b = client.getGroupSettings(groupType, groupName);
		assertSame(a, b);
		verify(groups).getGroupSettings(groupId);
		verifyNoMoreInteractions(groups);
	}
	
}
