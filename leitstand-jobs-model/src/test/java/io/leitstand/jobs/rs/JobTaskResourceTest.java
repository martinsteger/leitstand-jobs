package io.leitstand.jobs.rs;

import static io.leitstand.jobs.service.JobId.randomJobId;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.State.FAILED;
import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static javax.json.Json.createObjectBuilder;
import static org.mockito.Mockito.verify;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.messages.Messages;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobTaskService;
import io.leitstand.jobs.service.TaskId;

@RunWith(MockitoJUnitRunner.class)
public class JobTaskResourceTest {
    
    private static final JobId  JOB_ID  = randomJobId();
    private static final TaskId TASK_ID = randomTaskId();
    
    @Mock
    private JobTaskService service;
    
    @Mock
    private Messages messages;
    
    @InjectMocks
    private JobTaskResource resource = new JobTaskResource();
    
    @Test
    public void mark_task_as_completed_for_success_status_code() {
        JsonObject status = createObjectBuilder()
                            .add("status", 200)
                            .build();
        
        resource.updateTask(JOB_ID, 
                            TASK_ID, 
                            status);
        
        verify(service).updateTask(JOB_ID, TASK_ID, COMPLETED);
        
    }
    
    @Test
    public void mark_task_as_failed_for_error_status_code() {
        JsonObject status = createObjectBuilder()
                            .add("status", 500)
                            .build();
        
        resource.updateTask(JOB_ID, 
                            TASK_ID, 
                            status);
        
        verify(service).updateTask(JOB_ID, TASK_ID, FAILED);
    }
    
}
