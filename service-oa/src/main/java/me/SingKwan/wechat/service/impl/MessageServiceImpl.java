package me.SingKwan.wechat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import me.SingKwan.auth.service.SysUserService;
import me.SingKwan.model.process.Process;
import me.SingKwan.model.process.ProcessTemplate;
import me.SingKwan.model.system.SysUser;
import me.SingKwan.process.service.OaProcessService;
import me.SingKwan.process.service.OaProcessTemplateService;
import me.SingKwan.security.custom.LoginUserInfoHelper;
import me.SingKwan.wechat.service.MessageService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.wechat.service.impl
 * @className: MessageServiceImpl
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/31 16:35
 * @version: 1.0
 */
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private OaProcessService processService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OaProcessTemplateService processTemplateService;
    @Autowired
    private WxMpService wxMpService;
    //推送待审批人员
    @SneakyThrows
    @Override//这个是审批未处理时给待审批人员推送的消息
    public void pushPendingMessage(Long processId, Long userId, String taskId) {
        //查询流程信息
        Process process = processService.getBaseMapper().selectById(processId);
        //根据userId查询要推送人信息(传的是审批人的userId)
        SysUser sysUser = sysUserService.getBaseMapper().selectById(userId);
        //查询审批模板信息
        ProcessTemplate processTemplate = processTemplateService.getBaseMapper().selectById(process.getProcessTemplateId());
        //获取提交审批人的信息的(发起人的信息)
        SysUser submitSysUser = sysUserService.getBaseMapper().selectById(process.getUserId());


        //获取要给推送消息的审批者的openId(微信用户信息唯一标识)
        String openId=sysUser.getOpenId();
        if (StringUtils.isEmpty(openId)){
            //TODO 为了测试，添加默认值，当前自己的openId
            openId="o-5uM6K0N4wnbvPQ2RzjQ3T2wdqE";
            //实际流程里 只要用户扫码授权绑定了微信号码，且管理员先在数据库添加了用户的信息，
            // 则即可获取当前用户审批者的openId
        }
        //设置消息发送消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                //toUser需要填写用户的openId说明给哪个用户发送的
                .toUser(openId)
                //这里需要在微信创建的消息模板id
                .templateId("JGcwtq0R0BXSwEE8gCUw2_cz5SCmetCjI52V-1FjYa4")
                //点击消息，跳转到什么页面,这里是直接跳转到展示审批详情的审批处理页面
                .url("http://oasystemclienttest.viphk.91tunnel.com/#/show/"+processId+"/"+taskId)
                .build();

        //这里是从前端获取传回来的json字符串，并转成Map集合，遍历并进行拼接
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }

        //设置微信消息模板里面的参数值
        templateMessage.addData(new WxMpTemplateData("first", submitSysUser.getName()+"提交了"+processTemplate.getName()+"，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));

        //调用方法进行消息发送


        try {
           String msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            System.out.println(msg);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }


    }

//    @Override//这个是审批已处理后推送给下一个审批人的消息
//    public void pushProcessedMessage(Long processId, Long userId, Integer status) {
//        Process process = processService.getById(processId);
//        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
//        SysUser sysUser = sysUserService.getById(userId);
//        SysUser currentSysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
//        String openid = sysUser.getOpenId();
//        if(StringUtils.isEmpty(openid)) {
//            //此处没有多余的微信账号进行绑定，请自行绑定其他微信账号
//            openid = "omwf25izKON9dktgoy0dogqvnGhk";
//        }
//        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
//                .toUser(openid)//要推送的用户openid
//                .templateId("\tkV33sguF4C93ZE2iKfxWAit--mPY40b1AfwUnm_Yrqw")//模板id
//                .url("http://oasystemclienttest.viphk.91tunnel.com/#/show/"+processId+"/0")//点击模板消息要访问的网址
//                .build();
//        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
//        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
//        StringBuffer content = new StringBuffer();
//        for (Map.Entry entry : formShowData.entrySet()) {
//            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
//        }
//        templateMessage.addData(new WxMpTemplateData("first", "你发起的"+processTemplate.getName()+"审批申请已经被处理了，请注意查看。", "#272727"));
//        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
//        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
//        templateMessage.addData(new WxMpTemplateData("keyword3", currentSysUser.getName(), "#272727"));
//        templateMessage.addData(new WxMpTemplateData("keyword4", status == 1 ? "审批通过" : "审批拒绝", status == 1 ? "#009966" : "#FF0033"));
//        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
//        String msg = null;
//        try {
//            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
//            System.out.println(msg);
//        } catch (WxErrorException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
