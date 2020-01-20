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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;

public class TaskTest {
	
	private Job_Task from;
	private Job_Task to;
	
	@Before
	public void prepare_tasks(){
		from = new Job_Task();
		to = mock(Job_Task.class);
	}
	

	@Test
	public void notify_successor_about_new_predecessor(){
		from.addSuccessor(to);
		verify(to).onAddPredecessor(from);
		verifyNoMoreInteractions(to);
	}
	
	@Test
	public void notify_successor_about_removed_predecessor(){
		from.addSuccessor(to);
		from.removeSuccessor(to);
		verify(to).onRemovePredecessor(from);
		
	}
	
	@Test
	public void add_successor_does_nothing_if_transition_to_successor_already_exists(){
		from.addSuccessor(to);
		from.addSuccessor(to);
		from.addSuccessor(to);
		verify(to,times(1)).onAddPredecessor(from);
	}
	
	@Test
	public void remove_successor_does_nothing_if_transition_to_predecessor_does_not_exist(){
		from.addSuccessor(to);
		from.removeSuccessor(to);
		from.removeSuccessor(to);
		from.removeSuccessor(to);
		verify(to,times(1)).onRemovePredecessor(from);
	}
	
	@Test
	public void ignore_attempt_to_remove_a_transition_which_does_not_exist(){
		from.removeSuccessor(to);
		verifyZeroInteractions(to);
	}
	
}
