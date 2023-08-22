package me.SingKwan.auth.activiti;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth.activiti
 * @className: MyTaskListener
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/20 16:51
 * @version: 1.0
 */
public class MyTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        if (delegateTask.getName().equals("经理审批")){
            delegateTask.setAssignee("jack");
        }else if(delegateTask.getName().equals("人事审批")){
            delegateTask.setAssignee("tom");
        }
    }
}
