package me.SingKwan.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.SingKwan.model.process.ProcessTemplate;
import me.SingKwan.model.process.ProcessType;
import me.SingKwan.process.mapper.OaProcessTypeMapper;
import me.SingKwan.process.service.OaProcessTemplateService;
import me.SingKwan.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-20
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {

    @Autowired
    @Lazy
    private OaProcessTemplateService processTemplateService;

    @Override//查询所有审批分类和每个分类下所有的审批模板
    public List<ProcessType> findProcessType() {
        //1 查询所有审批分类，返回list集合
        List<ProcessType> processTypeList = this.getBaseMapper().selectList(null);
        //2 遍历返回的所有审批分类list集合
        for (ProcessType processType : processTypeList) {
            //3 得到每个审批分类，根据审批分类id查询对应审批模板
            LambdaQueryWrapper<ProcessTemplate> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(ProcessTemplate::getProcessTypeId,processType.getId());
            List<ProcessTemplate> processTemplates = processTemplateService.getBaseMapper().selectList(wrapper);
            //4 根据审批分类id查询对应审批模板数据（也是list集合），封装数据到每个审批分类对象里面去
            processType.setProcessTemplateList(processTemplates);

        }
        return processTypeList;

    }
}
