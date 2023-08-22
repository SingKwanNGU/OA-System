package me.SingKwan.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.SingKwan.model.process.ProcessTemplate;
import me.SingKwan.model.process.ProcessType;
import me.SingKwan.process.mapper.OaProcessTemplateMapper;
import me.SingKwan.process.service.OaProcessService;
import me.SingKwan.process.service.OaProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.SingKwan.process.service.OaProcessTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-20
 */
@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {



    @Autowired
    private OaProcessTypeService processTypeService;

    @Autowired
    private OaProcessService processService;

    @Override
    public IPage<ProcessTemplate> selectPageProcessTemplate(Page<ProcessTemplate> pageParam) {
        //1 调用mapper的方法实现分页查询
        Page<ProcessTemplate> processTemplatePage = this.getBaseMapper().selectPage(pageParam, null);
        //2 第一步分页查询返回分页数据，从分页数据获取列表list集合
        List<ProcessTemplate> processTemplateList = processTemplatePage.getRecords();
        //3 遍历list集合，得到每个对象的审批类型id
        for (ProcessTemplate processTemplate : processTemplateList) {
            Long processTypeId = processTemplate.getProcessTypeId();
            LambdaQueryWrapper<ProcessType> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(ProcessType::getId,processTypeId);
            //4 根据类型审批id,查询获取对应名称
            ProcessType processType = processTypeService.getOne(wrapper);
            if (null==processType){
                continue;
            }
            //5 完成最终封装processTypeName
            processTemplate.setProcessTypeName(processType.getName());
        }

        return processTemplatePage;
    }

    //修改模板发布状态 1表示已经发布
    //流程定义部署
    @Transactional
    @Override
    public void publish(Long id) {
        //修改模板发布状态 1表示已经发布
        ProcessTemplate processTemplate = this.getBaseMapper().selectById(id);
        processTemplate.setStatus(1);
        this.getBaseMapper().updateById(processTemplate);

        //流程定义部署-调用processService.deployByZip方法进行流程定义部署
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())){
            processService.deployByZip(processTemplate.getProcessDefinitionPath());
        }


    }
}
