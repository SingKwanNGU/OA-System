package me.SingKwan.auth.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth.activiti
 * @className: ProcessTestDemo
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/18 22:10
 * @version: 1.0
 */
@SpringBootTest
public class ProcessTestDemo {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private UserBean userBean;

    //-----------------------UEL-VALUE-------------------------

    @Test//部署流程定义
    public void deployProcess(){
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/OT.bpmn20.xml")
                .name("加班申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }

    @Test//启动流程实例
    public void startProcess(){
        Map<String, Object> variales=new HashMap<>();
        variales.put("assignee1","lucy");
        variales.put("assignee2","mary");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("OT", variales);
        System.out.println("流程定义id:"+processInstance.getProcessDefinitionId());
        System.out.println("流程实例id:"+processInstance.getId());
    }

    //查询个人流程任务
    @Test
    public void findTaskList(){
        String assignee="lucy";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee).list();
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }


    //------------------------UEL-METHOD-------------------------


    @Test
    public void deployProcess02() {
        // 流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/OT01.bpmn20.xml")
                .name("加班申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }



    /**
     * 启动流程实例
     */
    @Test
    public void startUpProcess02() {
        //创建流程实例,我们需要知道流程定义的key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("OT01");
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }

    //查询个人流程任务
    @Test
    public void findTaskList02() {
        String assignee = "lilei";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee).list();
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    //---------------------监听器分配任务--------------------------
    @Test
    public void deployProcess03() {
        // 流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/OT02.bpmn20.xml")
                .name("加班申请流程02")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }



    /**
     * 启动流程实例
     */
    @Test
    public void startUpProcess03() {
        //创建流程实例,我们需要知道流程定义的key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("OT02");
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }


    //查询个人流程任务
    @Test
    public void findTaskList03() {
        String assignee = "jack";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee).list();
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }


    //---------------Candidate-users---------------
    @Test//流程部署及启动流程实例
    public void deployProcess04() {
        // 流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/OT03.bpmn20.xml")
                .name("请假申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());

        //启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("OT03");
        System.out.println(processInstance.getId());
    }

    @Test//查询组任务
    public void findGroupTaskList() {
        //查询组任务
        List<Task> list = taskService.createTaskQuery()
                .taskCandidateUser("zhangsan01")//根据候选人查询
                .list();
        for (Task task : list) {
            System.out.println("----------------------------");
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }


    @Test//拾取组任务
    public void claimTask(){
        //拾取任务,即使该用户不是候选人也能拾取(建议拾取时校验是否有资格)
        //校验该用户有没有拾取任务的资格
        Task task = taskService.createTaskQuery()
                .taskCandidateUser("zhangsan02")//根据候选人查询
                .singleResult();
        if(task!=null){
            //拾取任务
            taskService.claim(task.getId(), "zhangsan02");
            System.out.println("任务拾取成功");
        }
    }

    @Test//查询个人待办任务
    public void findGroupPendingTaskList() {
        //任务负责人
        String assignee = "zhangsan01";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee)//只查询该任务负责人的任务
                .list();
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    @Test//办理个人任务
    public void completGroupTask() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("zhangsan02")  //要查询的负责人
                .singleResult();//返回一条
        taskService.complete(task.getId());
    }

    @Test//归还组任务
    public void assigneeToGroupTask() {
        String taskId = "fc3dbac1-26f9-11ee-8527-581122b79bfb";
        // 任务负责人
        String userId = "zhangsan01";
        // 校验userId是否是taskId的负责人，如果是负责人才可以归还组任务
        Task task = taskService
                .createTaskQuery()
                .taskId(taskId)
                .taskAssignee(userId)
                .singleResult();
        if (task != null) {
            // 如果设置为null，归还组任务,该 任务没有负责人
            taskService.setAssignee(taskId, null);
        }
    }

    @Test//任务交接
    public void assigneeToCandidateUser() {
        // 当前待办任务
        String taskId = "fc3dbac1-26f9-11ee-8527-581122b79bfb";
        // 校验zhangsan01是否是taskId的负责人，如果是负责人才可以归还组任务
        Task task = taskService
                .createTaskQuery()
                .taskId(taskId)
                .taskAssignee("zhangsan01")
                .singleResult();
        if (task != null) {
            // 将此任务交给其它候选人zhangsan02办理该 任务
            taskService.setAssignee(taskId, "zhangsan02");
        }
    }
}
