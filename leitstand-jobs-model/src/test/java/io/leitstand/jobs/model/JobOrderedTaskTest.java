package io.leitstand.jobs.model;

import static io.leitstand.inventory.service.ElementId.randomElementId;
import static io.leitstand.inventory.service.ElementName.elementName;
import static io.leitstand.inventory.service.ElementRoleName.elementRoleName;
import static io.leitstand.inventory.service.ElementSettings.newElementSettings;
import static io.leitstand.jobs.service.JobApplication.jobApplication;
import static io.leitstand.jobs.service.JobId.randomJobId;
import static io.leitstand.jobs.service.JobName.jobName;
import static io.leitstand.jobs.service.JobType.jobType;
import static io.leitstand.jobs.service.State.ACTIVE;
import static io.leitstand.jobs.service.State.COMPLETED;
import static io.leitstand.jobs.service.State.READY;
import static io.leitstand.jobs.service.State.WAITING;
import static io.leitstand.jobs.service.TaskId.randomTaskId;
import static io.leitstand.jobs.service.TaskName.taskName;
import static io.leitstand.jobs.service.TaskType.taskType;
import static io.leitstand.security.auth.UserName.userName;
import static io.leitstand.testing.ut.LeitstandCoreMatchers.hasSizeOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.inventory.service.ElementId;
import io.leitstand.inventory.service.ElementSettings;
import io.leitstand.jobs.service.JobApplication;
import io.leitstand.jobs.service.JobFlow;
import io.leitstand.jobs.service.JobId;
import io.leitstand.jobs.service.JobName;
import io.leitstand.jobs.service.JobTask;
import io.leitstand.jobs.service.JobTasks;
import io.leitstand.jobs.service.JobType;
import io.leitstand.security.auth.UserName;

@RunWith(MockitoJUnitRunner.class)
public class JobOrderedTaskTest {
    
    private static final JobId JOB_ID = randomJobId();
    private static final JobName JOB_NAME = jobName("job");
    private static final JobType JOB_TYPE  = jobType("type");
    private static final JobApplication JOB_APPLICATION = jobApplication("app");
    private static final UserName JOB_OWNER = userName("owner");
    
    
    @Mock
    private InventoryClient inventory;
    
    @Mock
    private JobProvider jobs;
    
    @InjectMocks
    private DefaultJobService service = new DefaultJobService();
    
    private ElementSettings leaf1;
    private ElementSettings leaf2;
    private ElementSettings spine1;
    private ElementSettings spine2;
    
    private Job_Task start;
    private Job_Task upgradeSpine1;
    private Job_Task pingSpine1;
    private Job_Task upgradeSpine2;
    private Job_Task pingSpine2;
    private Job_Task joinLeafUpgrades;
    private Job_Task upgradeLeaf1;
    private Job_Task pingLeaf1;
    private Job_Task upgradeLeaf2;
    private Job_Task pingLeaf2;
    private Job job;
    
    @Before
    public void create_job() {
        /* Mocks a job currently in progress.
                    
                          <start>
                             |
                 +-----------+-----------+       
                 |                       |
          <upgrade leaf1>         <upgrade leaf2>
                 |                       |
                 |                       | 
            <ping leaf1>            <ping leaf2>
                 |                       |
                 +-----------+-----------+
                             | 
                      <upgrade spine1>
                             |
                       <ping spine1>
                             | 
                      <upgrade spine2>
                             |
                       <ping spine2>
                             |
                            --- */
        
        // The dot output can be inspected at 
        // https://dreampuf.github.io/GraphvizOnline/
        
        // Mock element settings
        leaf1 = newElementSettings()
                .withElementRole(elementRoleName("accessleaf"))
                .withElementId(randomElementId())
                .withElementName(elementName("leaf1"))
                .build();
        
        leaf2 = newElementSettings()
                .withElementRole(elementRoleName("accessleaf"))
                .withElementId(randomElementId())
                .withElementName(elementName("leaf2"))
                .build();

        spine1 = newElementSettings()
                 .withElementRole(elementRoleName("spine"))
                 .withElementId(randomElementId())
                 .withElementName(elementName("spine1"))
                 .build();

        spine2 = newElementSettings()
                 .withElementRole(elementRoleName("spine"))
                 .withElementId(randomElementId())
                 .withElementName(elementName("spine2"))
                 .build();
        
        // Mock job and tasks
        job = new Job(JOB_APPLICATION, 
                      JOB_TYPE,
                      JOB_ID,
                      JOB_NAME, 
                      JOB_OWNER);
        
        
        start = new Job_Task(1L,
                             job, 
                             taskType("noop"),
                             randomTaskId(), 
                             taskName("start"));

        upgradeLeaf1 = new Job_Task(2L,
                                    job, 
                                    taskType("upgrade"),
                                    randomTaskId(), 
                                    taskName("Upgrade leaf1"),
                                    leaf1.getElementId());

        pingLeaf1 = new Job_Task(3L,
                                 job, 
                                 taskType("ping"),
                                 randomTaskId(), 
                                 taskName("Ping leaf1"),
                                 leaf1.getElementId());

        upgradeLeaf2 = new Job_Task(4L,
                                    job, 
                                    taskType("upgrade"),
                                    randomTaskId(), 
                                    taskName("Upgrade leaf2"),
                                    leaf2.getElementId());

        pingLeaf2 = new Job_Task(5L,
                                 job, 
                                 taskType("ping"),
                                 randomTaskId(), 
                                 taskName("Ping leaf2"),
                                 leaf2.getElementId()); 
        
        upgradeSpine1 = new Job_Task(6L,
                                     job, 
                                     taskType("upgrade"),
                                     randomTaskId(), 
                                     taskName("Upgrade spine1"),
                                     spine1.getElementId());

        pingSpine1 = new Job_Task(7L,
                                  job, 
                                  taskType("ping"),
                                  randomTaskId(), 
                                  taskName("Ping spine1"),
                                  spine1.getElementId());
        
        upgradeSpine2 = new Job_Task(8L,
                                     job, 
                                     taskType("upgrade"),
                                     randomTaskId(), 
                                     taskName("Upgrade spine2"),
                                     spine2.getElementId());

        pingSpine2 = new Job_Task(9L,
                                  job, 
                                  taskType("ping"),
                                  randomTaskId(), 
                                  taskName("Ping spine2"),
                                  spine2.getElementId());
        
        joinLeafUpgrades = new Job_Task(10L,
                                        job,
                                        taskType("noop"),
                                        randomTaskId(),
                                        taskName("join leaf upgrades"));
        joinLeafUpgrades.setCanary(true);
    
        start.addSuccessor(upgradeLeaf1);
        start.addSuccessor(upgradeLeaf2);
        upgradeLeaf1.addSuccessor(pingLeaf1);
        upgradeLeaf2.addSuccessor(pingLeaf2);
        pingLeaf1.addSuccessor(joinLeafUpgrades);
        pingLeaf2.addSuccessor(joinLeafUpgrades);
        joinLeafUpgrades.addSuccessor(upgradeSpine1);
        upgradeSpine1.addSuccessor(pingSpine1);
        pingSpine1.addSuccessor(upgradeSpine2);
        upgradeSpine2.addSuccessor(pingSpine2);
        
        // Fake job progress
        job.setJobState(ACTIVE);
        start.setTaskState(COMPLETED);
        upgradeLeaf1.setTaskState(COMPLETED);
        upgradeLeaf2.setTaskState(COMPLETED);
        pingLeaf1.setTaskState(READY);
        pingLeaf2.setTaskState(COMPLETED);
        joinLeafUpgrades.setTaskState(WAITING);
        upgradeSpine1.setTaskState(WAITING);
        upgradeSpine2.setTaskState(WAITING);
        pingSpine1.setTaskState(WAITING);
        pingSpine2.setTaskState(WAITING);
        
        job.setStart(start);
        when(jobs.fetchJob(JOB_ID)).thenReturn(job);
        
        // Mock inventory response
        Map<ElementId,ElementSettings> elements = new HashMap<>();
        elements.put(leaf1.getElementId(),leaf1);
        elements.put(leaf2.getElementId(),leaf2);
        elements.put(spine1.getElementId(),spine1);
        elements.put(spine2.getElementId(),spine2);
        
        when(inventory.getElements(job)).thenReturn(elements);

        when(inventory.getElementSettings(upgradeLeaf1)).thenReturn(leaf1);
        when(inventory.getElementSettings(pingLeaf1)).thenReturn(leaf1);
        when(inventory.getElementSettings(upgradeLeaf2)).thenReturn(leaf2);
        when(inventory.getElementSettings(pingLeaf2)).thenReturn(leaf2);
        when(inventory.getElementSettings(upgradeSpine1)).thenReturn(spine1);
        when(inventory.getElementSettings(pingSpine1)).thenReturn(spine1);
        when(inventory.getElementSettings(upgradeSpine2)).thenReturn(spine2);
        when(inventory.getElementSettings(pingSpine2)).thenReturn(spine2);
        
    }
    
    @Test
    public void compute_job_flow_graph() {
        
        JobFlow flow = service.getJobFlow(JOB_ID);

        assertEquals(JOB_ID,job.getJobId());
        assertEquals(JOB_APPLICATION,job.getJobApplication());
        assertEquals(JOB_TYPE,job.getJobType());
        assertEquals(JOB_NAME,job.getJobName());
        assertEquals(JOB_OWNER,job.getJobOwner());
        assertEquals(ACTIVE,job.getJobState());
        
        String dot = "digraph g {\n" + 
                "ordering=out\n" + 
                "tooltip=\"Job task execution flow \"\n" + 
                "splines=curves\n" + 
                "T0 [id=\""+start.getTaskId()+"\" shape=\"box\" style=\"rounded,filled\" color=\"#308720\" height=0.08 width=2.0 fixedsize=true label=\"\" tooltip=\"Barrier to wait for previous tasks before starting next task group\"];\n" + 
                "T1 [id=\""+upgradeLeaf1.getTaskId()+"\" shape=\"box\" fontname=\"Arial\" fontsize=\"11\" fontweight=\"bold\" fontcolor=\"white\" label=\"upgrade\n" + 
                "Upgrade leaf1\n"+
                "accessleaf\n"+ 
                "leaf1\n" + 
                "COMPLETED\" style=\"filled\" color=\"#308720\" tooltip=\"Task Upgrade leaf1\"];\n" + 
                "T2 [id=\""+pingLeaf1.getTaskId()+"\" shape=\"box\" fontname=\"Arial\" fontsize=\"11\" fontweight=\"bold\" fontcolor=\"black\" label=\"ping\n" + 
                "Ping leaf1\n"+
                "accessleaf\n"+ 
                "leaf1\n" + 
                "READY\" style=\"filled\" color=\"#A0A0A0\" tooltip=\"Task Ping leaf1\"];\n" + 
                "T3 [id=\""+joinLeafUpgrades.getTaskId()+"\" shape=\"box\" style=\"rounded,filled\" color=\"#A0A0A0\" height=0.08 width=2.0 fixedsize=true label=\"\" tooltip=\"Barrier to wait for previous tasks before starting next task group\"];\n" + 
                "T4 [id=\""+upgradeSpine1.getTaskId()+"\" shape=\"box\" fontname=\"Arial\" fontsize=\"11\" fontweight=\"bold\" fontcolor=\"white\" label=\"upgrade\n" + 
                "Upgrade spine1\n"+
                "spine\n"+
                "spine1\n" + 
                "WAITING\" style=\"filled\" color=\"#A0A0A0\" tooltip=\"Task Upgrade spine1\"];\n" + 
                "T5 [id=\""+pingSpine1.getTaskId()+"\" shape=\"box\" fontname=\"Arial\" fontsize=\"11\" fontweight=\"bold\" fontcolor=\"white\" label=\"ping\n" + 
                "Ping spine1\n"+
                "spine\n" + 
                "spine1\n"+
                "WAITING\" style=\"filled\" color=\"#A0A0A0\" tooltip=\"Task Ping spine1\"];\n" + 
                "T6 [id=\""+upgradeSpine2.getTaskId()+"\" shape=\"box\" fontname=\"Arial\" fontsize=\"11\" fontweight=\"bold\" fontcolor=\"white\" label=\"upgrade\n" + 
                "Upgrade spine2\n"+
                "spine\n"+
                "spine2\n" + 
                "WAITING\" style=\"filled\" color=\"#A0A0A0\" tooltip=\"Task Upgrade spine2\"];\n" + 
                "T7 [id=\""+pingSpine2.getTaskId()+"\" shape=\"box\" fontname=\"Arial\" fontsize=\"11\" fontweight=\"bold\" fontcolor=\"white\" label=\"ping\n" + 
                "Ping spine2\n"+
                "spine\n"+
                "spine2\n" + 
                "WAITING\" style=\"filled\" color=\"#A0A0A0\" tooltip=\"Task Ping spine2\"];\n" + 
                "T8 [id=\""+upgradeLeaf2.getTaskId()+"\" shape=\"box\" fontname=\"Arial\" fontsize=\"11\" fontweight=\"bold\" fontcolor=\"white\" label=\"upgrade\n" + 
                "Upgrade leaf2\n"+
                "accessleaf\n"+
                "leaf2\n"+ 
                "COMPLETED\" style=\"filled\" color=\"#308720\" tooltip=\"Task Upgrade leaf2\"];\n" + 
                "T9 [id=\""+pingLeaf2.getTaskId()+"\" shape=\"box\" fontname=\"Arial\" fontsize=\"11\" fontweight=\"bold\" fontcolor=\"white\" label=\"ping\n" + 
                "Ping leaf2\n"+
                "accessleaf\n"+ 
                "leaf2\n" + 
                "COMPLETED\" style=\"filled\" color=\"#308720\" tooltip=\"Task Ping leaf2\"];\n" + 
                "\n" + 
                "T6 -> T7 [penwidth=\"1\" color=\"#202020\"  style=\"solid\" tooltip=\"Next step\" arrowhead=open arrowsize=0.75];\n" + 
                "T5 -> T6 [penwidth=\"1\" color=\"#202020\"  style=\"solid\" tooltip=\"Next step\" arrowhead=open arrowsize=0.75];\n" + 
                "T4 -> T5 [penwidth=\"1\" color=\"#202020\"  style=\"solid\" tooltip=\"Next step\" arrowhead=open arrowsize=0.75];\n" + 
                "T3 -> T4 [penwidth=\"1\" color=\"#202020\"  style=\"dashed\" tooltip=\"Next step\" arrowhead=open arrowsize=0.75];\n" + 
                "T2 -> T3 [penwidth=\"1\" color=\"#202020\"  style=\"solid\" tooltip=\"Next step\" arrowhead=open arrowsize=0.75];\n" + 
                "T1 -> T2 [penwidth=\"1\" color=\"#202020\"  style=\"solid\" tooltip=\"Next step\" arrowhead=open arrowsize=0.75];\n" + 
                "T0 -> T1 [penwidth=\"1\" color=\"#202020\"  style=\"solid\" tooltip=\"Next step\" arrowhead=open arrowsize=0.75];\n" + 
                "T9 -> T3 [penwidth=\"1\" color=\"#202020\"  style=\"solid\" tooltip=\"Next step\" arrowhead=open arrowsize=0.75];\n" + 
                "T8 -> T9 [penwidth=\"1\" color=\"#202020\"  style=\"solid\" tooltip=\"Next step\" arrowhead=open arrowsize=0.75];\n" + 
                "T0 -> T8 [penwidth=\"1\" color=\"#202020\"  style=\"solid\" tooltip=\"Next step\" arrowhead=open arrowsize=0.75];\n" + 
                "}";
        // Print dot stdout to inspect it in online viewer
        // System.out.println(dot);
        
        assertEquals(dot,flow.getGraph());

    }
    
    @Test
    public void read_task_list() {
        JobTasks jobTasks = service.getJobTasks(JOB_ID);
        assertEquals(JOB_ID, jobTasks.getJobId());
        assertEquals(JOB_NAME, jobTasks.getJobName());
        assertEquals(JOB_TYPE, jobTasks.getJobType());
        assertEquals(JOB_APPLICATION, jobTasks.getJobApplication());
        assertEquals(ACTIVE, jobTasks.getJobState());
        
        List<JobTask> tasks = jobTasks.getTasks();
        
        assertThat(tasks,hasSizeOf(8));
        
        assertTaskEquals(upgradeLeaf1, 
                         tasks.get(0));
        assertTaskEquals(pingLeaf1, 
                         tasks.get(1));
        assertTaskEquals(upgradeLeaf2, 
                         tasks.get(2));
        assertTaskEquals(pingLeaf2, 
                         tasks.get(3));
        assertTaskEquals(upgradeSpine1, 
                         tasks.get(4));
        assertTaskEquals(pingSpine1, 
                         tasks.get(5));
        assertTaskEquals(upgradeSpine2, 
                         tasks.get(6));
        assertTaskEquals(pingSpine2, 
                         tasks.get(7));    
    }

    private void assertTaskEquals(Job_Task _task, JobTask task) {
        assertEquals(_task.getTaskId(),task.getTaskId());
        assertEquals(_task.getTaskName(),task.getTaskName());
        assertEquals(_task.getTaskType(),task.getTaskType());
        assertEquals(_task.getTaskState(),task.getTaskState());
        assertEquals(_task.getElementId(),task.getElementId());
        assertEquals(inventory.getElementSettings(_task).getElementName(),task.getElementName());
        assertEquals(inventory.getElementSettings(_task).getElementRole(),task.getElementRole());
    }
    
}
