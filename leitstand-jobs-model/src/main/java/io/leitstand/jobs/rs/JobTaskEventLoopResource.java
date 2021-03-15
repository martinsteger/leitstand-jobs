package io.leitstand.jobs.rs;

import static io.leitstand.commons.messages.MessageFactory.createMessage;
import static io.leitstand.jobs.rs.Scopes.ADM;
import static io.leitstand.jobs.rs.Scopes.ADM_JOB;
import static io.leitstand.jobs.service.ReasonCode.JOB0001I_JOB_EVENT_LOOP_STARTED;
import static io.leitstand.jobs.service.ReasonCode.JOB0002I_JOB_EVENT_LOOP_STOPPED;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.rs.Resource;
import io.leitstand.jobs.model.EventLoopStatus;
import io.leitstand.jobs.model.JobTaskEventLoop;
import io.leitstand.security.auth.Scopes;

@Resource
@Path("/tasks")
@Scopes({ADM, ADM_JOB})
public class JobTaskEventLoopResource {

    @Inject
    private JobTaskEventLoop service;
    
    @Inject
    private Messages messages;
    
    @POST
    @Path("/_start")
    public Messages startEventLoop() {
        service.startEventLoop();
        messages.add(createMessage(JOB0001I_JOB_EVENT_LOOP_STARTED));
        return messages;
    }
    
    @POST
    @Path("/_stop")
    public Messages stopEventLoop() {
        service.stopEventLoop();
        messages.add(createMessage(JOB0002I_JOB_EVENT_LOOP_STOPPED));
        return messages;
    }
    
    @GET
    @Path("/_status")
    public EventLoopStatus getStatus() {
        return service.getStatus();
    }
}
