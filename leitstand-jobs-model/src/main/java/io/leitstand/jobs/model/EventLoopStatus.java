package io.leitstand.jobs.model;

import static io.leitstand.commons.model.BuilderUtil.assertNotInvalidated;

import java.util.Date;

public class EventLoopStatus {

    /**
     * Create a new job eventloop status.
     * @return a builder for an immutable job event loop status object.
     */
    public static Builder newJobEventLoopStatus() {
        return new Builder();
    }
    
    /**
     * Builder for immutable job status value object.
     */
    public static class Builder {
        
        private EventLoopStatus status = new EventLoopStatus();
        
        /**
         * Sets whether the job event loop is enable or disabled.
         * @param enabled <code>true</code> if the loop is stared, <code>false</code> otherwise.
         * @return a reference to this builder to continue object creation
         */
        public Builder withEnabled(boolean enabled) {
            assertNotInvalidated(getClass(), status);
            status.enabled = enabled;
            return this;
        }
        
        /**
         * Sets the timestamp of the last job operational state change.
         * @param dateModified timestamp of last job operational state change.
         * @return a reference to this builder to continue object creation
         */
        public Builder withDateModified(Date dateModified) {
            assertNotInvalidated(getClass(), status);
            status.dateModified = new Date(dateModified.getTime());
            return this;
        }
            
        /**
         * Returns an immutable job event loop state.
         * @return an immutable job event loop state.
         */
        public EventLoopStatus build() {
            try {
                assertNotInvalidated(getClass(), status);
                return status;
            } finally {
                this.status = null;
            }
        }
        
    }
    
    
    private boolean enabled;
    private Date dateModified;
    
    /**
     * Returns whether the job event loop is enabled.
     * @return <code>true</code> when the job event loop is enabled, <code>false</code> otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Returns the timestamp of the last event loop operational state change.
     * @return the timestamp of the last event loop operational state change.
     */
    public Date getDateModified() {
        if(dateModified == null) {
            return null;
        }
        return new Date(dateModified.getTime());
    }
    
    
}
