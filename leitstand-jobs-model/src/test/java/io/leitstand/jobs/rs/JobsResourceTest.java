package io.leitstand.jobs.rs;

import static io.leitstand.commons.jsonb.IsoDateAdapter.isoDateFormat;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.messages.Messages;
import io.leitstand.jobs.service.JobQuery;
import io.leitstand.jobs.service.JobService;

@RunWith(MockitoJUnitRunner.class)
public class JobsResourceTest {

    @Mock
    private Messages messages;
    
    @Mock
    private JobService service;
    
    @InjectMocks
    private JobsResource resource = new JobsResource();
    

    
    
    @Test
    public void search_running_jobs_scheduled_in_given_timerange() {
        ArgumentCaptor<JobQuery> queryCaptor = ArgumentCaptor.forClass(JobQuery.class);
        doReturn(emptyList()).when(service).findJobs(queryCaptor.capture());
        
        long now = currentTimeMillis();
        Date from = new Date(now - 60000);
        Date to = new Date(now);
        
        resource.getJobs("filter", "true", isoDateFormat(from), isoDateFormat(to));
        
        JobQuery query = queryCaptor.getValue();
        assertEquals("filter",query.getFilter());
        assertTrue(query.isRunningOnly());
        assertEquals(from,query.getScheduledAfter());
        assertEquals(to,query.getScheduledBefore());
    }
    
    
    @Test
    public void filtered_job_list() {
        ArgumentCaptor<JobQuery> queryCaptor = ArgumentCaptor.forClass(JobQuery.class);
        doReturn(emptyList()).when(service).findJobs(queryCaptor.capture());
        
        resource.getJobs("filter", null, null, null);
        
        JobQuery query = queryCaptor.getValue();
        assertEquals("filter",query.getFilter());
        assertFalse(query.isRunningOnly());
        assertNull(query.getScheduledAfter());
        assertNull(query.getScheduledBefore());
    }
    
}
