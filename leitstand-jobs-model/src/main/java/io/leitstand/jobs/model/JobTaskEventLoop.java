package io.leitstand.jobs.model;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Logger.getLogger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.leitstand.jobs.service.TaskId;

@ApplicationScoped
public class JobTaskEventLoop extends BaseEventLoop{

    private static final Logger LOG = getLogger(JobTaskEventLoop.class.getName());
    
    private static final long MAX_WAIT_TIME_SECONDS = 30;
    
    private AtomicInteger availableHandlers = new AtomicInteger(10);
    
    @Resource
    private ManagedExecutorService wm;
    
    @Inject
    private TaskSchedulerService scheduler;
    
    private Pause pause;
    
    @Override
    public void onStartup() {
        pause = new Pause(MAX_WAIT_TIME_SECONDS,
                          SECONDS);
        super.onStartup();
    }

    @Override
    public void run() {
        try {
            LOG.info("Job task event loop started.");
            
            while(isActive()) {
                scheduleTasks();
            }
            
            LOG.info("Job task event loop stopped.");
        } catch (Exception e) {
            LOG.fine(() -> "Job task event loop crashed: "+e.getMessage());
            stopEventLoop();
            startEventLoop();
        }
    }

    protected void scheduleTasks() throws InterruptedException{
        int limit = availableHandlers.get();
        if(limit > 0) {
            List<TaskId> tasks = scheduler.fetchExecutableTasks(limit);
            if(!tasks.isEmpty()) {
                // Reset wait time to eagerly schedule tasks eligible for execution.
                pause.reset(); 
                
                // Schedule all tasks
                for(TaskId taskId : tasks) {
                    availableHandlers.decrementAndGet();
                    wm.execute(() -> {
                        try {
                            scheduler.executeTask(taskId);
                        } finally {
                            availableHandlers.incrementAndGet();
                        }
                    });
                }

                return;
            }
        }
        pause.sleep();
    }

}
