package io.leitstand.jobs.model;

import static io.leitstand.inventory.service.ElementId.randomElementId;
import static io.leitstand.jobs.service.State.ACTIVE;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.State.READY;
import static io.leitstand.jobs.service.State.WAITING;
import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static io.leitstand.jobs.service.TaskName.taskName;
import static io.leitstand.jobs.service.TaskType.taskType;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import io.leitstand.jobs.service.TaskId;
import io.leitstand.jobs.service.TaskName;
import io.leitstand.jobs.service.TaskType;

public class Job_TaskTest {

    private static final TaskType TASK_TYPE = taskType("unit-test");
    private static final TaskId TASK_ID = randomTaskId();
    private static final TaskName TASK_NAME = taskName("unit-test-task");
    
    
    private Job_Task task;
    private Job job;
    
    @Before
    public void createTask() {
        job = mock(Job.class);
        
        task = new Job_Task(job, TASK_TYPE, TASK_ID, TASK_NAME);
    }
    
    @Test
    public void ready_task_is_eligible_for_execution() {
        task.setTaskState(READY);
        assertTrue(task.isEligibleForExecution());
    }
    
    @Test
    public void waiting_tasks_with_no_predecessors_is_eligible_for_execution() {
        task.setTaskState(WAITING);
        assertTrue(task.isEligibleForExecution());
    }

    @Test
    public void waiting_tasks_with_completed_predecessors_is_eligible_for_execution() {
        task.setTaskState(WAITING);
        Job_Task predecessor = new Job_Task(job,
                                            TASK_TYPE,
                                            randomTaskId(),
                                            TASK_NAME);
        predecessor.setTaskState(COMPLETED);
        predecessor.addSuccessor(task);
        assertTrue(task.isEligibleForExecution());
    }

    @Test
    public void waiting_tasks_with_uncompleted_predecessors_is_not_eligible_for_execution() {
        task.setTaskState(WAITING);
        Job_Task predecessor = new Job_Task(job,
                                            TASK_TYPE,
                                            randomTaskId(),
                                            TASK_NAME);
        predecessor.setTaskState(ACTIVE);
        predecessor.addSuccessor(task);
        assertFalse(task.isEligibleForExecution());   
    }
    
    @Test
    public void task_with_no_successor_is_not_declared_as_fork_task() {
        assertFalse(task.isForkTask());
    }
    
    @Test
    public void task_with_one_successor_is_not_a_forking_task() {
        Job_Task successor = mock(Job_Task.class);
        task.addSuccessor(successor);
        assertFalse(task.isForkTask());
    }
    
    @Test
    public void task_with_more_than_one_successor_is_a_forking_task() {
        Job_Task successorA = mock(Job_Task.class);
        Job_Task successorB = mock(Job_Task.class);
        task.addSuccessor(successorA);
        task.addSuccessor(successorB);
        assertTrue(task.isForkTask());
    }
    
    @Test
    public void task_without_element_uuid_is_not_an_element_task() {
        assertFalse(task.isElementTask());
    }
    
    @Test
    public void task_with_element_uuid_is_not_an_element_task() {
        task = new Job_Task(job, TASK_TYPE, TASK_ID, TASK_NAME,randomElementId());
        assertTrue(task.isElementTask());
    }
    
    
}
