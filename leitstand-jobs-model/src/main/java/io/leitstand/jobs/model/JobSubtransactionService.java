package io.leitstand.jobs.model;

import javax.inject.Inject;
import javax.inject.Provider;

import io.leitstand.commons.model.Repository;
import io.leitstand.commons.model.Service;
import io.leitstand.commons.tx.SubtransactionService;

@Service
@Jobs
public class JobSubtransactionService extends SubtransactionService {

    @Inject
    @Jobs
    private Provider<SubtransactionService> provider;
    
    @Inject
    @Jobs
    private Repository repository;
    
    @Override
    protected Repository getRepository() {
        return repository;
    }

    @Override
    protected Provider<SubtransactionService> getServiceProvider() {
        return provider;
    }

}
