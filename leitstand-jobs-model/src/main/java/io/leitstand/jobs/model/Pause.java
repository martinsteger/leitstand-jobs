package io.leitstand.jobs.model;

import static java.lang.Math.min;

import java.util.concurrent.TimeUnit;

public class Pause {
    
    private final long maxWaitTimeMillis;
    private long waitTimeMillis;

    public Pause(long max, TimeUnit unit) {
        this.waitTimeMillis = 1000;
        this.maxWaitTimeMillis = unit.toMillis(max);
    }
    
    public void sleep() throws InterruptedException {
        Thread.sleep(waitTimeMillis);
        waitTimeMillis = min(2*waitTimeMillis,maxWaitTimeMillis);
    }
    
    public void reset() {
        waitTimeMillis = 1000;
    }
    
    
}
