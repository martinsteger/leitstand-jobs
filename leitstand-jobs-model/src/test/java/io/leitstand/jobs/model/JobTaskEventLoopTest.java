package io.leitstand.jobs.model;

import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.concurrent.ManagedExecutorService;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import io.leitstand.jobs.service.TaskId;

@RunWith(MockitoJUnitRunner.class)
public class JobTaskEventLoopTest {

    @Mock
    private Pause pause;
    
    @Mock
    private ManagedExecutorService wm;
    
    @Mock
    private AtomicInteger workers = new AtomicInteger();
    
    @Mock
    private TaskSchedulerService scheduler;
    
    @InjectMocks
    private JobTaskEventLoop loop = new JobTaskEventLoop();
    
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
    public void pause_loop_when_all_worker_threads_are_occupied() throws InterruptedException {
        loop.scheduleTasks();
        verify(pause).sleep();
        verifyZeroInteractions(scheduler);
    }
    
    @Test
    public void pause_loop_when_no_tasks_are_eligible_for_execution() throws InterruptedException {
        workers.set(1);
        when(scheduler.fetchExecutableTasks(1)).thenReturn(emptyList());
        
        loop.scheduleTasks();
        verify(pause).sleep();
        verify(pause,never()).reset();
    }
    
    @Test
    public void schedule_task_and_reset_pause() throws InterruptedException  {
        TaskId task = randomTaskId();
        workers.set(1);
        when(scheduler.fetchExecutableTasks(1)).thenReturn(asList(task));
        executeRunnable().when(wm).execute(any(Runnable.class));
        
        loop.scheduleTasks();
        verify(pause).reset();
        verify(pause,never()).sleep();
        verify(scheduler).executeTask(task);
    }

    private Stubber executeRunnable() {
        return doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Runnable r = (Runnable) invocation.getArguments()[0];
                r.run();
                return null;
            }
        });
    }
}
