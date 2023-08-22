package me.SingKwan.process.service.impl;

import me.SingKwan.auth.service.SysUserService;
import me.SingKwan.model.process.ProcessRecord;
import me.SingKwan.model.system.SysUser;
import me.SingKwan.process.mapper.OaProcessRecordMapper;
import me.SingKwan.process.service.OaProcessRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.SingKwan.security.custom.LoginUserInfoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 审批记录 服务实现类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-25
 */
@Service
public class OaProcessRecordServiceImpl extends ServiceImpl<OaProcessRecordMapper, ProcessRecord> implements OaProcessRecordService {

    @Autowired
    private SysUserService sysUserService;
    @Override
    public void record(Long processId, Integer status, String description) {
        Long userId = LoginUserInfoHelper.getUserId();
        SysUser user = sysUserService.getById(userId);
        ProcessRecord processRecord=new ProcessRecord();
        processRecord.setProcessId(processId);
        processRecord.setStatus(status);
        processRecord.setDescription(description);
        processRecord.setOperateUser(user.getUsername());
        processRecord.setOperateUserId(userId);
        this.getBaseMapper().insert(processRecord);
    }
}
