package io.leitstand.jobs.rs;

import static io.leitstand.jobs.service.JobId.randomJobId;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.messages.Messages;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobService;

@RunWith(MockitoJUnitRunner.class)
public class JobResourceTest {

    private static final JobId JOB_ID = randomJobId();
    
    @Mock
    private Messages messages;
    
    @Mock
    private JobService service;
    
    @InjectMocks
    private JobResource resource = new JobResource();
    
    @Test
    public void cancel_job() {
        resource.cancelJob(JOB_ID);
        verify(service).cancelJob(JOB_ID);
    }

    @Test
    public void resume_job() {
        resource.resumeJob(JOB_ID);
        verify(service).resumeJob(JOB_ID);
    }
    
    @Test
    public void confirm_job() {
        resource.confirmJob(JOB_ID);
        verify(service).confirmJob(JOB_ID);
    }
    
    @Test
    public void remove_job() {
        resource.removeJob(JOB_ID);
        verify(service).removeJob(JOB_ID);
    }

    

    

    
    

    
}
