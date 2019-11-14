package com.atguigu.gmall.alisms.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.alisms.service.SendMessageService;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("alisms")
@RestController
public class SendMessage {
    @Autowired
    private SendMessageService sendMessageService;

    @GetMapping("sendMessage")
    public Resp<Object> SendMessage(String mobile){
        if(StringUtils.isEmpty(mobile)){
            return Resp.fail("请输入手机号");
        }
        String str = this.sendMessageService.SendMessage(mobile);
        return Resp.ok(str);
    }
}
