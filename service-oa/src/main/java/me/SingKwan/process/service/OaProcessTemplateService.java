package me.SingKwan.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.SingKwan.model.process.ProcessTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批模板 服务类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-20
 */
public interface OaProcessTemplateService extends IService<ProcessTemplate> {
    //分页查询审批模板，把审批类型对应名称查询
    IPage<ProcessTemplate> selectPageProcessTemplate(Page<ProcessTemplate> pageParam);

    //修改模板发布状态 1表示已经发布
    //流程定义部署
    void publish(Long id);
}
