package me.SingKwan.process.service;

import me.SingKwan.model.process.ProcessType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-20
 */
public interface OaProcessTypeService extends IService<ProcessType> {
    //查询所有审批分类和每个分类下所有的审批模板
    List<ProcessType> findProcessType();
}
