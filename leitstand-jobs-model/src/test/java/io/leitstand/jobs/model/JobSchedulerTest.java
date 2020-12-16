package io.leitstand.jobs.model;

import static io.leitstand.commons.model.ObjectUtil.asSet;
import static io.leitstand.jobs.service.JobId.randomJobId;
import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static io.leitstand.testing.ut.LeitstandCoreMatchers.hasSizeOf;
import static io.leitstand.testing.ut.LeitstandCoreMatchers.isEmptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.Repository;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.TaskId;

@RunWith(MockitoJUnitRunner.class)
public class JobSchedulerTest {
    
    private static final JobId JOB_ID = randomJobId();
    private static final TaskId START_TASK_ID = randomTaskId();
    private static final TaskId TASK_A_ID = randomTaskId();
    private static final TaskId TASK_B_ID = randomTaskId();
    
    @Mock
    private Repository repository;
    
    @InjectMocks
    private JobScheduler scheduler = new JobScheduler();
    
    
    @Test
    public void return_start_task_when_no_task_of_the_job_has_been_completed() {
        Job job = mock(Job.class);
        Job_Task start = mock(Job_Task.class);
        
        when(start.getTaskId()).thenReturn(START_TASK_ID);
        when(job.getStart()).thenReturn(start);
        when(repository.execute(any(Query.class))).thenReturn(job);
        
        List<TaskId> tasks = scheduler.activateExecutableTasks(JOB_ID);
        assertThat(tasks,hasSizeOf(1));
        assertEquals(START_TASK_ID,tasks.get(0));
        
    }
    
    @Test
    public void return_task_successors_eligible_for_execution() {
        Job job = mock(Job.class);
        Job_Task start = mock(Job_Task.class);
        Job_Task a = mock(Job_Task.class);
        Job_Task b = mock(Job_Task.class);
        when(a.getTaskId()).thenReturn(TASK_A_ID);
        when(b.getTaskId()).thenReturn(TASK_B_ID);
        when(b.isEligibleForExecution()).thenReturn(true);
        
        Set<Job_Task> successors = asSet(a,b);
        
        when(job.getStart()).thenReturn(start);
        when(start.isSucceeded()).thenReturn(true);
        when(repository.execute(any(Query.class))).thenReturn(job)
                                                  .thenReturn(successors);
        
        List<TaskId> tasks = scheduler.activateExecutableTasks(JOB_ID);
        assertThat(tasks,hasSizeOf(1));
        assertEquals(TASK_B_ID,tasks.get(0));
        
    }
    
    @Test
    public void return_empty_list_if_no_task_is_eligible_for_execution() {
        Job job = mock(Job.class);
        Job_Task start = mock(Job_Task.class);
        Job_Task a = mock(Job_Task.class);
        Job_Task b = mock(Job_Task.class);
        when(a.getTaskId()).thenReturn(TASK_A_ID);
        when(b.getTaskId()).thenReturn(TASK_B_ID);
        
        Set<Job_Task> successors = asSet(a,b);
        
        when(job.getStart()).thenReturn(start);
        when(start.isSucceeded()).thenReturn(true);
        when(repository.execute(any(Query.class))).thenReturn(job)
                                                  .thenReturn(successors);
        
        List<TaskId> tasks = scheduler.activateExecutableTasks(JOB_ID);
        assertThat(tasks,isEmptyList());
        
    }
    
}
