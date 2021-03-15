/*
 * Copyright 2020 RtBrick Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.leitstand.jobs.model;

import static io.leitstand.jobs.model.EventLoopStatus.newJobEventLoopStatus;
import static java.util.logging.Level.FINER;
import static java.util.logging.Logger.getLogger;

import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;

import io.leitstand.commons.ShutdownListener;
import io.leitstand.commons.StartupListener;

public abstract class BaseEventLoop implements Runnable, StartupListener, ShutdownListener{
	
	private static final Logger LOG = getLogger(BaseEventLoop.class.getName());
	
	private Date dateModified;
	private volatile boolean active;
	
	@Resource
	private ManagedExecutorService wm;
	
	@Override
	public void onStartup() {
		startEventLoop();
	}

	@Override
	public void onShutdown() {
		stopEventLoop();
	}
	
	public void stopEventLoop() {
	    this.dateModified = new Date();
		this.active = false;
	}
	
    public EventLoopStatus getStatus() {
        return newJobEventLoopStatus()
               .withEnabled(active)
               .withDateModified(dateModified)
               .build();
    }
	
	public void startEventLoop() {
		if(!active) {
		    this.dateModified = new Date();
			active = true;
			try {
				execute(this);
			} catch (Exception e) {
				LOG.severe("Unable to start job event loop: "+e);
				LOG.log(FINER,e.getMessage(),e);
			}
		}
	}
	
	protected void execute(Runnable r) {
	    wm.execute(r);
	}
	
	public boolean isActive() {
        return active;
    }
}

