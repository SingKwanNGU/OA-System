package me.SingKwan.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.SingKwan.model.process.Process;
import com.baomidou.mybatisplus.extension.service.IService;
import me.SingKwan.vo.process.ApprovalVo;
import me.SingKwan.vo.process.ProcessFormVo;
import me.SingKwan.vo.process.ProcessQueryVo;
import me.SingKwan.vo.process.ProcessVo;

import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-22
 */
public interface OaProcessService extends IService<Process> {
    //审批管理列表
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);

    //部署流程定义
    void deployByZip(String deployPath);
    //启动流程实例
    void startUp(ProcessFormVo processFormVo);
    //查询待处理的列表
    IPage<ProcessVo> findPending(Page<Process> pageParam);
    //查询申请信息
    Map<String, Object> show(Long id);
    //审批
    void approve(ApprovalVo approvalVo);
    //已处理
    IPage<ProcessVo> findProcessed(Page<Process> pageParam);
    //已发起
    IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam);
}
