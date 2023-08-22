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

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth.activiti
 * @className: ProcessTestGateway
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/20 21:50
 * @version: 1.0
 */
@SpringBootTest
public class ProcessTestGateway {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Test//部署流程定义
    public void deployProcess(){
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/qingjia002.bpmn20.xml")
                .name("请假申请流程002")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }

    @Test//启动流程实例
    public void startProcess(){
        Map<String, Object> variales=new HashMap<>();
        variales.put("day","3");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia002", variales);
        System.out.println("流程定义id:"+processInstance.getProcessDefinitionId());
        System.out.println("流程实例id:"+processInstance.getId());
    }

    //查询个人流程任务
    @Test
    public void findTaskList(){
        String assignee="gousheng";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee).list();
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    //处理流程任务
    @Test
    public void completeTask(){
        Task task = taskService.createTaskQuery()
                .taskAssignee("xiaocui")//查询任务的负责人
                .singleResult();//返回一条结果
        //完成任务，参数：任务Id
        taskService.complete(task.getId());

    }
}
