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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class JoinTest {

	private Job_Task a;
	private Job_Task b;
	private Job_Task ab;
	
	@Before
	public void setup_graph(){
		a = new Job_Task();
		b = new Job_Task();
		ab = new Job_Task();
		
	}
	
	@Test
	public void add_join_as_successor_of_given_predecessors(){
		ab.join(a,b);
		assertTrue(a.isPredecessorOf(ab));
		assertTrue(b.isPredecessorOf(ab));
		assertTrue(ab.isSuccessorOf(a));
		assertTrue(ab.isSuccessorOf(b));
		
	}
	
	@Test
	public void remove_join_as_successor_also_removes_predecessor_from_predecessors_list(){
		ab.join(a,b);
		a.removeSuccessor(ab);
		assertFalse(a.isPredecessorOf(ab));
		assertTrue(b.isPredecessorOf(ab));
		assertFalse(ab.isSuccessorOf(a));
		assertTrue(ab.isSuccessorOf(b));
		
	}
	
	
}
