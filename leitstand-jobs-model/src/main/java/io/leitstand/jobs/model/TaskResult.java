package io.leitstand.jobs.model;

import static io.leitstand.jobs.service.State.ACTIVE;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.State.FAILED;
import static io.leitstand.jobs.service.State.REJECTED;

import io.leitstand.jobs.service.State;

public class TaskResult {

    public static TaskResult failed() {
        return failed(null);
    }
    
    public static TaskResult failed(String message) {
        return new TaskResult(FAILED,message);
    }

    public static TaskResult rejected() {
        return failed(null);
    }
    
    public static TaskResult rejected(String message) {
        return new TaskResult(REJECTED,message);
    }

    
    public static TaskResult completed() {
        return new TaskResult(COMPLETED,null);
    }
    
    public static TaskResult active() {
        return new TaskResult(ACTIVE,null);
    }
    
    private TaskResult(State taskState, String message) {
        this.taskState = taskState;
        this.message = message;
    }
    
    
    private State taskState;
    private String message;
    
    public State getTaskState() {
        return taskState;
    }
    
    public String getMessage() {
        return message;
    }
    
}
