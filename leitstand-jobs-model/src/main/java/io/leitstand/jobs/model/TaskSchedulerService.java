package io.leitstand.jobs.model;

import static io.leitstand.commons.db.DatabaseService.prepare;
import static io.leitstand.jobs.model.Job_Task.findTaskById;
import static io.leitstand.jobs.service.TaskId.taskId;

import java.util.List;

import javax.inject.Inject;

import io.leitstand.commons.db.DatabaseService;
import io.leitstand.commons.model.Repository;
import io.leitstand.commons.model.Service;
import io.leitstand.jobs.service.TaskId;

@Service
public class TaskSchedulerService {

    @Inject
    @Jobs
    private DatabaseService db;
    
    
    @Inject
    @Jobs
    private Repository repository;
    
    @Inject
    private TaskProcessingService service;
    

    public List<TaskId> fetchExecutableTasks(int limit){
        String sql = "UPDATE job.job_task "+
                     "SET state='ACTIVE' "+
                     "WHERE id IN ( "+
                       "SELECT id "+
                       "FROM job.job_task "+
                       "WHERE state='READY' "+
                       "FOR UPDATE SKIP LOCKED "+
                       "LIMIT ?"+
                     ") "+
                     "RETURNING uuid";
                     
        return db.executeQuery(prepare(sql,limit), 
                               rs -> taskId(rs.getString(1)));
    }
    
    public void executeTask(TaskId taskId) {
        Job_Task task = repository.execute(findTaskById(taskId));
        service.executeTask(task);
    }
    
    
}
