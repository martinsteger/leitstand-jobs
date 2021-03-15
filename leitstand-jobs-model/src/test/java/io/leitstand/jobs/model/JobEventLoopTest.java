package io.leitstand.jobs.model;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.concurrent.ManagedExecutorService;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobEventLoopTest {

    @Mock
    private Pause pause;
    
    @Mock
    private ManagedExecutorService wm;
    
    @Mock
    private JobScheduler scheduler;
    
    @InjectMocks
    private JobEventLoop loop = new JobEventLoop();
    
    @After
    public void stopEventLoop() {
        loop.stopEventLoop();
    }
    
    @Test
    public void start_event_loop() {
        loop.startEventLoop();
        verify(wm).execute(loop);
    }
    
    @Test
    public void do_not_start_event_loop_twice() {
        loop.startEventLoop();
        loop.startEventLoop();
        verify(wm,times(1)).execute(loop);
    }
    
    @Test
    public void pause_loop_when_no_jobs_and_no_tasks_are_eligible_for_execution() throws InterruptedException {
        loop.scheduleJobs();
        verify(pause).sleep();
        verify(pause,never()).reset();

    }
    
    @Test
    public void dont_pause_loop_when_jobs_eligible_for_execution_exist() throws InterruptedException {
        when(scheduler.startScheduledJobs()).thenReturn(1);
        
        loop.scheduleJobs();
        verify(pause,never()).sleep();
        verify(pause).reset();
    }
    
    @Test
    public void dont_pause_loop_when_tasks_eligible_for_execution_exist() throws InterruptedException {
        when(scheduler.markTasksEligibleForExecution()).thenReturn(1);
        
        loop.scheduleJobs();
        verify(pause,never()).sleep();
        verify(pause).reset();
    }
    
}
