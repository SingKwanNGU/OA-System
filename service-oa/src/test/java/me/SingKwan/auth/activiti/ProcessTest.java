package me.SingKwan.auth.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth.activiti
 * @className: ProcessTest
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/18 18:20
 * @version: 1.0
 */
@SpringBootTest
public class ProcessTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;


    @Test//单个文件部署
    public void deployProcess(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/process.bpmn20.xml")
                .addClasspathResource("process/qingjia.png")
                .name("请假申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }


    //压缩包部署
    @Test
    public void deployProcessByZip() {
        // 定义zip输入流
        InputStream inputStream = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream(
                        "process/qingjia.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        // 流程部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .name("请假申请流程")
                .deploy();
        System.out.println("流程部署id：" + deployment.getId());
        System.out.println("流程部署名称：" + deployment.getName());
    }


    //启动流程实例
    @Test
    public void  startProcess(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
        System.out.println("流程定义Id:"+processInstance.getProcessDefinitionId());
        System.out.println("流程实例Id:"+processInstance.getId());
        System.out.println("当前活动Id:"+processInstance.getActivityId());

    }

    //查询流程任务
    @Test
    public void findTaskList(){
        String assignee="lisi";
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
                .taskAssignee("lisi")//查询任务的负责人
                .singleResult();//返回一条结果
        //完成任务，参数：任务Id
        taskService.complete(task.getId());

    }

    //查询已处理历史任务
    @Test
    public void findProcessedTaskList(){
        //zhangsan已处理过的历史任务
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee("zhangsan")
                .finished().list();
        for (HistoricTaskInstance historicTaskInstance : list) {
            System.out.println("流程实例Id:"+historicTaskInstance.getProcessInstanceId());
            System.out.println("任务Id:"+historicTaskInstance.getId());
            System.out.println("任务负责人:"+historicTaskInstance.getAssignee());
            System.out.println("任务名称:"+historicTaskInstance.getName());
        }
    }

    /**
     * 启动流程实例，添加businessKey
     */
    @Test
    public void startUpProcessAddBusinessKey(){
        String businessKey = "1";
        // 启动流程实例，指定业务标识businessKey，也就是请假申请单id
        ProcessInstance processInstance = runtimeService.
                startProcessInstanceByKey("process",businessKey);
        // 输出
        System.out.println("业务id:"+processInstance.getBusinessKey());
    }

    //全部流程实例挂起  流程定义类似定义了java类 流程实例类似定义了java类的对象
    @Test //这是直接挂起了流程定义 所有的流程实例都会挂起
    public void suspendProcessInstance() {
        ProcessDefinition process = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("process")
                .singleResult();
        // 获取到当前流程定义是否为暂停状态 suspended方法为true是暂停的，suspended方法为false是运行的
        boolean suspended = process.isSuspended();
        if (suspended) {
            // 暂停,那就可以激活
            // 参数1:流程定义的id  参数2:是否激活    参数3:时间点
            repositoryService.activateProcessDefinitionById(process.getId(), true, null);
            System.out.println("流程定义:" + process.getId() + "激活");
        } else {
            repositoryService.suspendProcessDefinitionById(process.getId(), true, null);
            System.out.println("流程定义:" + process.getId() + "挂起");
        }
    }

    //单个流程实例挂起
    @Test //这里只是挂起单个流程实例，对整个流程定义中的其他实例不产生影响。
    public void SingleSuspendProcessInstance(){
        String processInstanceId="6e9ecf3f-2565-11ee-8fbb-581122b79bfb";
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        //获取到当前流程定义是否为暂停状态   suspended方法为true代表为暂停   false就是运行的
        boolean suspended = processInstance.isSuspended();
        if (suspended) {
            // 暂停,那就可以激活
            // 参数1:流程定义的id  参数2:是否激活    参数3:时间点
            runtimeService.activateProcessInstanceById(processInstanceId);
            System.out.println("流程实例:" + processInstanceId + "激活");
        } else {
            runtimeService.suspendProcessInstanceById(processInstanceId);
            System.out.println("流程实例:" + processInstanceId + "挂起");
        }
    }



}
