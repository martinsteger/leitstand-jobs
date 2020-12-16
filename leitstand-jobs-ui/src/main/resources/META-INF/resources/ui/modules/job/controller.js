/*
 *  (c) RtBrick, Inc - All rights reserved, 2015 - 2017
 */
import {Controller,Menu} from '/ui/js/ui.js';
import {UIElement,Control} from '/ui/js/ui-components.js';
import {Jobs,Job,Task} from './job.js';

let jobs = new Jobs();

class Progressbar extends UIElement {
	renderDom(){
		let tasks = this.viewModel.getProperty('tasks');
		if(tasks){
			let done = tasks.filter(task => task.task_state == 'COMPLETED').length;
			let active = tasks.filter(task => task.task_state == 'ACTIVE').length;
			this.innerHTML=`<div class="progress">
								<p>${(done/tasks.length*100).toFixed(0)} %</p>
								<div id="progressbar" class="progressbar">
									<div class="completed" style="width:${done/tasks.length*100}%;">&nbsp;</div>
									<div class="active" style="width:${active/tasks.length*100}%;">&nbsp;</div>
								</div>	
							</div>`;
		}
		
	}
}
customElements.define('job-progress',Progressbar);

class Taskflow extends UIElement {
	renderDom(){
		
		
		
		this.requires({'libs':['/ui/js/ext/svg/svg-pan-zoom.min.js',
			  	  	           '/ui/js/ext/graph/viz.js' ]})
			.then(()=>{this.innerHTML=Viz(this.viewModel.getProperty('graph'))});

	}
}
customElements.define('job-taskflow',Taskflow);

class Editor extends Control {
    connectedCallback(){
        const config = this.viewModel.getProperty(this.binding)||{};
        this.innerHTML=`<textarea>${JSON.stringify(config,null,' ')}</textarea>`;
        const editor = CodeMirror.fromTextArea(this.querySelector("textarea"), {
            lineNumbers: true,
            styleActiveLine: true,
            matchBrackets: true,
            mode:{name:'javascript', json:true}
        });
        this.form.addEventListener('UIPreExecuteAction',() => {
            this.viewModel.setProperty(this.binding,JSON.parse(editor.getValue()));
        });
    }
}
customElements.define('task-editor',Editor);


let overviewController = function(){
	return new Controller({
		resource:jobs,
		viewModel:function(jobs){
			let  viewModel = {};
			viewModel.query = this.location.params;
			if(!viewModel.query.before){
				viewModel.query.before = new Date();
				viewModel.before=false;
			} else {
				 viewModel.before=true;
			}
			if(!viewModel.query.after){
				viewModel.query.after = new Date(viewModel.query.before.getTime() - 24*60*60*1000);
				viewModel.after = false;
			} else {
				viewModel.after = true;
			}
			viewModel.jobs = jobs;
			return viewModel;
		},
		refresh:function(){
			this.reload();
		},
		buttons:{
			'filter':function(){
				this.reload(this.getViewModel('query'));
			},
		}
	});
};

let jobController = function() {
	let job = new Job({'scope':'settings'});
	return new Controller({
		resource:job,
		viewModel:function(settings){
			settings.suspend = function(){
				return settings.schedule.date_suspend != null;
			};
			settings.disabled = function(){
	 			return (settings.job_state == 'READY' ||  settings.job_state == 'FAILED') ? '' : 'disabled';
			};
			settings.default_date_suspend = settings.schedule.date_suspend ? new Date(settings.schedule.date_suspend) : new Date(new Date(settings.schedule.date_scheduled).getTime()+4*3600*1000);
			return settings;
		},
		buttons:{
			'save-settings':function(){
				let settings = this.getViewModel();
				// Update schedule if start immediately was selected
				if(settings.start_mode == 'immediate'){
					settings.schedule.date_scheduled = (new Date()).toISOString();
				}
				if(this.input('suspend').isChecked()){
					// Set auto-resume
					settings.schedule.auto_resume = this.input('auto_resume').isChecked();
				} else {
					// Disable suspend option
					delete settings.schedule.date_suspend;
				}
				job.updateSettings(this.location.params,
								   settings);
			}
		}
	});
};

let tasksController = function() {
	let job = new Job();
	return new Controller({//TODO Refactor with UI components integration
		resource:job,
		viewModel:function(job){
			let viewModel = job;
			viewModel.actions = [];
			if(job.job_state == 'CANCELLED' || job.job_state == 'FAILED'){
				viewModel.actions.push({'action':'resume',
										'style':'primary',
										'label':'Resume',
										'role':'Operator'});	
			}
		
			if(job.job_state == 'CONFIRM'){
				viewModel.actions.push({'action':'confirm',
									 	'style':'primary',
										'label':'Confirm',
										'role':'Operator'});	
			}	
		
			if(job.job_state == 'ACTIVE' || job.job_state == 'READY' || job.job_state == 'CONFIRM'){
				viewModel.actions.push({'action':'cancel',
										'style':'danger',
										'label':'Cancel',
										'role':'Operator'});	
			}
		
			if(job.job_state == 'CANCELLED' || job.job_state == 'COMPLETED' || job.job_state == 'FAILED'){
				viewModel.actions.push({'action':'remove',
							 			'style':'danger',
							 			'label':'Remove',
		   		   		   	   			'role':'Operator'});	
			}
		
			// Check whether at least on task is bound to an element.
			viewModel.render_element_column = function(){
					for(let i=0; i < job.tasks.length; i++){
						if(job.tasks[i].element_id){
							return true;
						}
					}
					return false;
			};
			
			viewModel.in_progress = function(){
				return job.job_state === 'ACTIVE' || job.job_state == 'CONFIRM';
			}
			return viewModel;
		},
		buttons:{
			'cancel':function(){
				job.cancel(this.location.params);
			},
			'confirm':function(){
				job.confirm(this.location.params);
			},
			'resume':function(){
				job.resume(this.location.params);
			},
			'remove':function(){
				this.navigate({'view':'confirm-remove.html',
						   	   '?':this.location.params});
			}
		},
		'onSuccess':function(){
			this.load();
		},
		'refresh':function(){
			if(this.getViewModel()["job_state"]=='COMPLETED'){
				return;
			}
			job.load(this.location.params)
			   .then((settings) => {
			       this.init(settings);
			   });
		}
	});
};

let confirmRemoveController = function() {
	let job = new Job();
	return new Controller({
		resource:job,
		buttons:{
			'confirm-remove':function(){
				job.remove(this.location.params);
			}
		},
		'onSuccess':function(){
			this.navigate('jobs.html');
		}
	});
};

const flowController = function() {
	const flow = new Job({'scope':'flow'});
	return new Controller({
		resource:flow,
		'refresh':function(){
			this.reload();
		}
	});
};

const taskController = function() {
	const task = new Task();
	return new Controller({
		resource:task,
		viewModel:function(task){
		    task.json=function(){
		        return JSON.stringify(task.parameters,null,'  ');
		    }
		    task.editable=function(){
		        return task.task_state == 'FAILED';
		    }
		},
		buttons:{
		    'save-task':function(){
		        task.setParameter(this.location.params,this.getViewModel("parameters"));
		    }
		}
	});
};

const tasksMenu = {
    'master':tasksController(),
    'details':{
        'task.html':taskController()
    }
        
}


export const menu = new Menu({"jobs.html":overviewController(),
						 	  "job.html":jobController(),
						 	  "confirm-remove.html":confirmRemoveController(),
						 	  "tasks.html":tasksMenu,
						 	  "taskflow.html" : flowController()},
						 	  "/ui/views/job/jobs.html");

