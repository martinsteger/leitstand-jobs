/*
 *  (c) RtBrick, Inc - All rights reserved, 2015 - 2017
 */

// ES6 migration backlog
// TODO Update jsdoc

import {Resource} from '/ui/js/client.js';


/**
 * Creates a new job collection.
 * @constructor
 * @param {string} [cfg.filter] - a default filter criterion
 * @classdesc
 * The Jobs resource provides access to the scheduler's collection of jobs. 
 * Loads all jobs matching the given filter criterion with a default limit of at most 100 jobs.
 * @augments Resource
 */
export class Jobs extends Resource {
	
	constructor(cfg) {
		super();
		this._cfg = cfg;
	}
	
	/**
	 * Loads all jobs matching the given filter.
	 * @param {string} [params.filter] The job filter query 
	 * @param {number} [params.limit=100] The maximum number of fetched jobs
	 */
	load(params) {
		return this.json("/api/v1/jobs?filter={{&filter}}&running={{&running}}&after={{&after}}&before={{&before}}",
		          		 this._cfg,
		          		 params)
		           .GET();
	}
}

/**
 * Creates a new task resource.
 * @constructor
 * @param {string} cfg.group The group's UUID or the group's unique name.
 * @param {string} cfg.job The job's UUID
 * @param {string} cfg.task The task's UUID
 * @classedesc
 * The Task resource provides access to a task
 * of a scheduled job.
 * A task forms the smallest unit of a job. A task represents a certain operation
 * performed on an element.
 * @augments Resource
 */
export class Task extends Resource{
	
	constructor(cfg) {
		super();
		this._cfg = cfg;
	}
	
	/**
	 * Loads the task from the job scheduler.
	 * @param {string} [cfg.group] The group's UUID or the group's unique name.
	 * @param {string} [cfg.job] The job's UUID
	 * @param {string} [cfg.task] The task's UUID
	 */
	load(link) {
		return this.json("/api/v1/jobs/{{&job}}/tasks/{{&task}}",
						 this._cfg,
						 link)
				   .GET();
	}

	setParameter(params,taskParams){
	    return this.json("/api/v1/jobs/{{&job}}/tasks/{{&task}}/parameters",params)
	               .PUT(taskParams);     
	}
	
}

/**
 * Creates a new Job resource.
 * @constructor
 * @param {string} [cfg.group] The group's UUID or the group's unique name
 * @param {string} [cfg.job] The job's UUID
 * @param {string} [cfg.scope] The scope defines what information of the job should be fetched
 * @classdesc
 * The job resources provides access to a scheduled job. Provides function to fetch the current state of 
 * a job as well as to update the schedule of the job, unless the job is already running. In this case, the
 * job can be cancelled.
 * @augments Resource
 */
export class Job extends Resource {
	
	constructor(cfg) {
		super();
		this._cfg = cfg
	}
	
	/**
	 * Loads the scheduled job.
	 * @param {string} [param.group] The group's UUID or the group's unique name
	 * @param {string} [param.job] The job's UUID
	 */
	load(params) {
		return this.json("/api/v1/jobs/{{&job}}/{{&scope}}",
						 this._cfg,
						 params)
				   .GET();
	}
	
	/**
	 * Loads the scheduled job.
	 * @param {string} [param.group] The group's UUID or the group's unique name
	 * @param {string} [param.job] The job's UUID
	 * @param {Object} settings The job's settings as stated in EMS REST API
	 */
	updateSettings(params,settings){
		return this.json("/api/v1/jobs/{{&job}}/settings",
						 this._cfg,
						 params)
				   .PUT(settings);
	}
	
	/**
	 * Cancels the scheduled job.
	 * @param {string} [param.group] The group's UUID or the group's unique name
	 * @param {string} [param.job] The job's UUID
	 */
	cancel(params) {
		return this.json("/api/v1/jobs/{{&job}}/_cancel",
						 this._cfg,
						 params)
				   .POST();
	}
	
	//TODO JSDoc
	confirm(params) {
		return this.json("/api/v1/jobs/{{&job}}/_confirm",
						 this._cfg,
						 params)
				   .POST();
	};
	
	resume(params) {
		return this.json("/api/v1/jobs/{{&job}}/_resume",
						 this._cfg,
						 params)
				   .POST();
	}	
	
	remove(params){
		return this.json("/api/v1/jobs/{{&job}}",
						 this._cfg,
						 params)
				   .DELETE();
	}
}	