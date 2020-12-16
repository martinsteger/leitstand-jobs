package io.leitstand.jobs.model;

import static io.leitstand.jobs.model.Job.findJobById;
import static io.leitstand.jobs.service.ReasonCode.JOB0100E_JOB_NOT_FOUND;
import static java.lang.String.format;

import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.model.Repository;
import io.leitstand.jobs.service.JobId;

@Dependent
public class JobProvider {

    private static final Logger LOG = Logger.getLogger(JobProvider.class.getName());
    
    @Inject
    @Jobs
    private Repository repository;

    protected JobProvider() {
        // CDI
    }
    
    protected JobProvider(Repository repository) {
        this.repository = repository;
    }

    
    public Job fetchJob(JobId jobId) {
        Job job = repository.execute(findJobById(jobId));
        if(job == null) {
            LOG.fine(() -> format("%s: Job %s not found.",
                                  JOB0100E_JOB_NOT_FOUND.getReasonCode(),
                                  jobId));
            throw new EntityNotFoundException(JOB0100E_JOB_NOT_FOUND,jobId);
        }
        return job;
    }
    
    public Job tryFetchJob(JobId jobId) {
        return repository.execute(findJobById(jobId));
    }
    
    
}
