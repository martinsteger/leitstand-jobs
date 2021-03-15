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

import static io.leitstand.commons.db.DatabaseService.prepare;

import javax.inject.Inject;

import io.leitstand.commons.db.DatabaseService;
import io.leitstand.commons.model.Service;

@Service
public class JobScheduler {
	
    @Inject
    @Jobs
	private DatabaseService db;
	
	public int startScheduledJobs() {
	    String sql = "UPDATE job.job "+
	                 "SET state='ACTIVE' "+
	                 "WHERE state='READY' "+
	                 "AND tsschedule <= NOW()";
	    return db.executeUpdate(prepare(sql));	    
	}
	
	public int markTasksEligibleForExecution(){
	    String sql = "WITH "+ 
	                 "active_jobs(id) AS ( "+
	                   "SELECT id "+ 
	                   "FROM job.job "+ 
	                   "WHERE state='ACTIVE' "+
	                 "), "+
	                 "blocked_tasks(id) AS ( "+
	                   "SELECT t.id "+
	                   "FROM job.job_task t "+
	                   "JOIN job.job_task_transition tr "+
	                   "ON tr.to_task_id = t.id "+
	                   "JOIN job.job_task p "+ 
	                   "ON tr.from_task_id = p.id "+
	                   "WHERE t.state = 'WAITING' "+
	                   "AND p.state <> 'COMPLETED' "+
	                   "AND t.job_id IN (SELECT id FROM active_jobs) "+
	                 ") "+
	                 "UPDATE job.job_task t "+
	                 "SET state='READY' "+
	                 "WHERE t.state='WAITING' "+
	                 "AND t.id NOT IN (SELECT id FROM blocked_tasks) "+
	                 "AND t.job_id IN (SELECT id FROM active_jobs)";
	    return db.executeUpdate(prepare(sql));
	}

	public int markCompletedJobs() {
	    String sql = "WITH completed_jobs AS ( "+
	                   "SELECT j.id FROM job.job j "+
	                   "LEFT JOIN job.job_task t "+ 
	                   "ON j.id = t.job_id "+
	                   "AND t.state <> 'COMPLETED' "+
	                   "WHERE j.state = 'ACTIVE' "+
	                   "AND t.id IS NULL "+
	                 ") "+
	                 "UPDATE job.job j "+
	                 "SET state = 'COMPLETED' "+
	                 "FROM completed_jobs c "+
	                 "WHERE j.id = c.id "+
	                 "AND j.state = 'ACTIVE'";
	    return db.executeUpdate(prepare(sql));
	}

    public int markJobsWaitingForConfirmation() {
        String sql = "WITH jobs_to_confirm AS ( "+
                       "SELECT DISTINCT j.id FROM job.job j "+
                       "JOIN job.job_task t "+ 
                       "ON j.id = t.job_id "+
                       "AND t.state = 'CONFIRM' "+
                       "WHERE j.state = 'ACTIVE' "+
                     ") "+
                     "UPDATE job.job j "+
                     "SET state = 'CONFIRM' "+
                     "FROM jobs_to_confirm c "+
                     "WHERE j.id = c.id "+
                     "AND j.state = 'ACTIVE'";
        return db.executeUpdate(prepare(sql));
    }
	
	
	public int markFailedJobs() {
        String sql = "WITH failed_jobs AS ( "+
                       "SELECT DISTINCT j.id FROM job.job j "+
                       "JOIN job.job_task t "+ 
                       "ON j.id = t.job_id "+
                       "AND t.state = 'FAILED' "+
                       "WHERE j.state = 'ACTIVE' "+
                     ") "+
                     "UPDATE job.job j "+
                     "SET state = 'FAILED' "+
                     "FROM failed_jobs c "+
                     "WHERE j.id = c.id "+
                     "AND j.state = 'ACTIVE'";
        return db.executeUpdate(prepare(sql));
	}
	
}
