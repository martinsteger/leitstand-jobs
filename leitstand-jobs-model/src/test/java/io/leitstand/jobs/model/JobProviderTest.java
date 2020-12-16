package io.leitstand.jobs.model;

import static io.leitstand.jobs.service.JobId.randomJobId;
import static io.leitstand.jobs.service.ReasonCode.JOB0100E_JOB_NOT_FOUND;
import static io.leitstand.testing.ut.LeitstandCoreMatchers.reason;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.Repository;

@RunWith(MockitoJUnitRunner.class)
public class JobProviderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    @Mock
    private Repository repository;
    
    @InjectMocks
    private JobProvider jobs = new JobProvider();
    
    @Test
    public void fetch_job_returns_job_from_repository() {
        Job job = mock(Job.class);
        when(repository.execute(any(Query.class))).thenReturn(job);
        
        assertSame(job,jobs.fetchJob(randomJobId()));
    }
    
    @Test
    public void fetch_job_with_custom_locking_returns_job_from_repository() {
        Job job = mock(Job.class);
        when(repository.execute(any(Query.class))).thenReturn(job);
        
        assertSame(job,jobs.fetchJob(randomJobId()));
    }
    
    @Test
    public void fetch_job_throws_EntityNotFoundException_for_unknown_job_id() {
        exception.expect(EntityNotFoundException.class);
        exception.expect(reason(JOB0100E_JOB_NOT_FOUND));
        
        jobs.fetchJob(randomJobId());
    }
    
    @Test
    public void try_fetch_job_returns_null_for_unknown_job_id() {
        assertNull(jobs.tryFetchJob(randomJobId()));
    }
    
}
