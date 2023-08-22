package me.SingKwan.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonParser;
import me.SingKwan.auth.service.SysUserService;
import me.SingKwan.model.process.Process;
import me.SingKwan.model.process.ProcessRecord;
import me.SingKwan.model.process.ProcessTemplate;
import me.SingKwan.model.system.SysUser;
import me.SingKwan.process.mapper.OaProcessMapper;
import me.SingKwan.process.service.OaProcessRecordService;
import me.SingKwan.process.service.OaProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.SingKwan.process.service.OaProcessTemplateService;
import me.SingKwan.security.custom.LoginUserInfoHelper;
import me.SingKwan.vo.process.ApprovalVo;
import me.SingKwan.vo.process.ProcessFormVo;
import me.SingKwan.vo.process.ProcessQueryVo;
import me.SingKwan.vo.process.ProcessVo;
import me.SingKwan.wechat.service.MessageService;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-22
 */
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {

    @Lazy
    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private OaProcessRecordService processRecordService;

    @Autowired
    private HistoryService historyService;

    @Lazy
    @Autowired
    private MessageService messageService;

    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pageModel = this.getBaseMapper().selectPage(pageParam, processQueryVo);
        return pageModel;

    }

    @Override
    public void deployByZip(String deployPath) {
        //定义zip输入流
        InputStream inputStream=this.getClass()
                .getClassLoader()
                .getResourceAsStream(deployPath);
        ZipInputStream zipInputStream=new ZipInputStream(inputStream);

        //流程部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    //启动流程实例
    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //1 根据当前用户id获取用户信息
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        //2 根据审批模板id把模板信息获取
        Long processTemplateId = processFormVo.getProcessTemplateId();
        ProcessTemplate processTemplate = processTemplateService.getBaseMapper().selectById(processTemplateId);
        //3 保存提交审批信息到业务表，oa_process
        Process process =new Process();
        //把processFormVo的属性赋值到process中
        BeanUtils.copyProperties(processFormVo,process);
        //set方法设置一下processFormVo里没有而process中有的属性。
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);
        this.getBaseMapper().insert(process);
        //4 启动流程实例
        //4.1 流程定义key
        String processDefinitionKey = processTemplate.getProcessDefinitionKey();
        //4.2 业务key processId
        String businessKey = String.valueOf(process.getId());
        //4.3 流程参数form表单数据json数据，转换成map集合
        //拿到整个表单json
        String formValues = processFormVo.getFormValues();
        //再从中拿到formData
        //把整个表单json转回去json
        JSONObject jsonObject = JSON.parseObject(formValues);
        //得到json格式的formData
        JSONObject formData = jsonObject.getJSONObject("formData");
        //遍历formData得到内容并储存在map中
        Map<String,Object> map=new HashMap<>();
        for (Map.Entry<String, Object> entry : formData.entrySet()) {
            map.put(entry.getKey(),entry.getValue());
        }
        Map<String,Object> variables=new HashMap<>();
        variables.put("data",map);

        //调用方法启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);

        //5 查询下一个审批人
        List<Task> taskList=this.getCurrentTaskList(processInstance.getId());
        List<String> nameList=new ArrayList<>();
        for (Task task : taskList) {
            String assignee = task.getAssignee();
            SysUser user = sysUserService.getUserByUsername(assignee);
            String name = user.getName();
            nameList.add(name);
            //6 推送消息
            messageService.pushPendingMessage(process.getId(), user.getId(), task.getId());
        }




        //7 业务和流程关联 更新oa_process数据
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待"+ StringUtils.join(nameList.toArray(),",") +"审批");
        this.getBaseMapper().updateById(process);

        //记录操作审批信息记录
        processRecordService.record(process.getId(),1,"发起申请");
    }

    //查询待处理的列表
    @Override
    public IPage<ProcessVo> findPending(Page<Process> pageParam) {
        //1 封装查询条件，根据当前登录的用户名称
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .orderByTaskCreateTime()
                .desc();
        //2 调用方法分页条件查询，返回list集合，待办任务集合
        //listPage方法有两个参数
        //第一个参数：开始位置begin(每页都要) 第二个参数： 每页显示记录数size=limit
        int begin= (int) ((pageParam.getCurrent()-1)*pageParam.getSize());
        int size= (int) pageParam.getSize();
        List<Task> taskList = query.listPage(begin, size);
        long totalCount = query.count();

        //3 封装返回的list集合数据 到 List<ProcessVo>中
        //思路：从list集合获取task对象，从task对象到processInstanceId再到流程实例processInstance
        //processInstance->业务key->流程对象process,查oa_process表获取其他属性
        List<ProcessVo> processVoList=new ArrayList<>();
        for (Task task : taskList) {
            //从task对象到processInstanceId
            String processInstanceId = task.getProcessInstanceId();
            //从processInstanceId到流程实例processInstance
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            String businessKey = processInstance.getBusinessKey();
            if (businessKey==null){
                continue;
            }


            Long processId=Long.parseLong(businessKey);
            Process process=this.getBaseMapper().selectById(processId);

            ProcessVo processVo=new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId(task.getId());
            processVoList.add(processVo);
        }

        //封装返回分页Page<ProcessVo>对象
        //分页无非六个参数
        //1 每页数量limit/size
        //2 当前页current
        //3 总记录数totalCount
        //4 总页数totalPage
        //5 分页数据record/data
        //6 数据总数dataTotal
        IPage<ProcessVo> page=new Page<>(pageParam.getCurrent()
                , pageParam.getSize(),totalCount);
        page.setRecords(processVoList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {
        //1 根据流程id获取流程process，从而获得process信息
        Process process = this.getBaseMapper().selectById(id);
        //2 根据流程id获取流程记录信息
        LambdaQueryWrapper<ProcessRecord> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId,id);
        List<ProcessRecord> processRecordList = processRecordService.getBaseMapper().selectList(wrapper);
        //3 根据模板id查询模板信息
        Long processTemplateId = process.getProcessTemplateId();
        ProcessTemplate processTemplate = processTemplateService.getBaseMapper().selectById(processTemplateId);

        //4 判断当前用户是否可以审批
        //可以看到信息不一定能审批，不能重复审批
        Boolean isApprove=false;
        List<Task> taskList=this.getCurrentTaskList(process.getProcessInstanceId());
        for (Task task : taskList) {
            //判断任务审批人是否包含当前登录用户
            String username =LoginUserInfoHelper.getUsername();
            if(task.getAssignee().equals(username)){
                isApprove=true;
            }
        }

        //5 查询数据封装到map集合并返回
        Map<String,Object> map=new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        map.put("isApprove", isApprove);
        return map;
    }

    //审批
    @Override
    public void approve(ApprovalVo approvalVo) {
        //1 从approvalVo中获取任务id,根据任务id获取流程变量
        String taskId = approvalVo.getTaskId();
        Map<String, Object> variables = taskService.getVariables(taskId);
        //遍历查看流程变量
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        //2 判断审批状态值
        if(approvalVo.getStatus()==1){
            //2.1 状态值=1 审批通过
            Map<String,Object> variable=new HashMap<>();
            taskService.complete(taskId,variable);
        }else {
            //2.2 状态值=-1 审批驳回，流程直接结束
            this.endTask(taskId);

        }
        //3 记录审批相关过程信息 oa_process_record
        //调用OaProcessRecord的record方法
        String description=approvalVo.getStatus().intValue()==1?"已通过":"驳回";
        processRecordService.record(approvalVo.getProcessId(),approvalVo.getStatus(),description);


        //4 查询下一个审批人，更新流程表记录process表记录
        //通过processId从而获取流程实例
        Process process = this.getBaseMapper().selectById(approvalVo.getProcessId());
        //查询任务
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(taskList)){
            List<String> assignList=new ArrayList<>();
            for (Task task : taskList) {
                //根据登录用户名获取用户真实姓名
                String assignee=task.getAssignee();
                SysUser user = sysUserService.getUserByUsername(assignee);
                String name = user.getName();
                assignList.add(name);

//                //TODO 公众号给下一个审批人进行消息推送
//                messageService.pushProcessedMessage(process.getId(), user.getId(),process.getStatus() );

            }
            //更新process流程信息
            //Status==1审批中 2审批通过 -1驳回
            process.setDescription("等待"+ StringUtils.join(assignList.toArray(),",") +"进行审批");
            process.setStatus(1);
        }else {
            //判断status值进行更新description
            if(approvalVo.getStatus().intValue()==1){
                process.setDescription("审批完成(通过)");
                process.setStatus(2);
            }else {
                process.setDescription("审批完成(驳回)");
                process.setStatus(-1);
            }
        }
        //更新process对象
        this.getBaseMapper().updateById(process);

    }


    //已处理
    @Override
    public IPage<ProcessVo> findProcessed(Page<Process> pageParam) {
        //封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .finished()
                .orderByTaskCreateTime().desc();
        //调用方法条件分页查询，返回list集合
        int begin=(int)((pageParam.getCurrent()-1)*pageParam.getSize());
        int size= (int) pageParam.getSize();
        long totalCount = query.count();
        List<HistoricTaskInstance> historicTaskInstances = query.listPage(begin, size);
        //遍历返回list集合，封装成list<processVo>
        List<ProcessVo> processVoList=new ArrayList<>();
        for (HistoricTaskInstance taskInstance : historicTaskInstances) {
            //获取流程实例id
            String processInstanceId = taskInstance.getProcessInstanceId();
            //查oa_process表获取process对象，复制属性
            LambdaQueryWrapper<Process> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId,processInstanceId);
            Process process = this.getBaseMapper().selectOne(wrapper);
            ProcessVo processVo=new ProcessVo();
            if (process==null){
                continue;
            }
            BeanUtils.copyProperties(process,processVo);
            processVoList.add(processVo);
        }

        //IPage封装分页查询所有数据
        IPage<ProcessVo> pageModel=new Page<ProcessVo>(pageParam.getCurrent(), pageParam.getSize(),totalCount);
        pageModel.setRecords(processVoList);
        return pageModel;

    }

    //已发起
    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo=new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> pageModel = this.getBaseMapper().selectPage(pageParam, processQueryVo);
        return pageModel;
    }

    private void endTask(String taskId) {
        //1 根据任务id获取任务对象 task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //2 获取流程定义模型BpmnModel，调用getBpmnModel方法(传入流程定义id)
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        //3 获取结束流向节点(也可以获取startEvent,userTask,sequenceFlow)
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        if(CollectionUtils.isEmpty(endEventList)){
            return;
        }
        //FlowNode继承FlowElement EndEvent继承Event Event继承FlowNode
        //FlowElement->FlowNode->Event->EndEvent
        FlowNode endFlowNode=(FlowNode) endEventList.get(0);

        //4 当前流向节点,调用getFlowElement方法(传入任务节点名/任务ID,每个任务都有字段，相当于任务Id)
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
        //5 清理当前流动方向,调用getOutgoingFlows()方法获取当前流动方向，在调用clear()方法
        //  临时保存当前活动的原始方向，以防万一
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        currentFlowNode.getOutgoingFlows().clear();
        //6 创建新流向(创建新流向sequenceflow对象，给当前节点和结束节点进行连线)
        SequenceFlow sequenceFlow=new SequenceFlow();
        sequenceFlow.setId("newSequenceFlow");
        sequenceFlow.setSourceFlowElement(currentFlowNode);
        sequenceFlow.setTargetFlowElement(endFlowNode);
        //7 当前节点指向新方向(修改当前节点的流动方向，指向新流向)
        //流向可以有多个，放入多少个流向就有多少个流向方向
        List newSequenceFlowList=new ArrayList<>();
        newSequenceFlowList.add(sequenceFlow);
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //8 完成当前任务(只是修改了流向，任务依旧调用complete方法)
        taskService.complete(taskId);
    }

    //获取当前任务列表
    private List<Task> getCurrentTaskList(String id) {
        List<Task> list = taskService.createTaskQuery().processInstanceId(id).list();
        return list;
    }
}
