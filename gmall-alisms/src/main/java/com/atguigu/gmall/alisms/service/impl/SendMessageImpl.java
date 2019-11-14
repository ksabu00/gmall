package com.atguigu.gmall.alisms.service.impl;

import com.atguigu.gmall.alisms.service.SendMessageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendMessageImpl implements SendMessageService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String SendMessage(String mobile) {
        // 2.判断电话号码是否符合格式要求
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        if (mobile.length() != 11) {
            return "手机号码格式不正确";
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(mobile);
            if (!m.matches()) {
                return "手机号码格式不正确";
            }
        }
// 用于存验证码键 例如：code:12312312:code
        String codeStr = "code:"+ mobile + ":code";

        // 用于存获取验证码次数的键 例如：code:12323123:count
        String codeContStr = "code:" + mobile + ":count";

        // 3.判断手机号码指定时间段内获取短信验证的次数是否超过3次
        String countStr = redisTemplate.opsForValue().get(codeContStr);
        int count = 0;
        if (!StringUtils.isEmpty(countStr)) {
            count = Integer.parseInt(countStr);
        }

        if (count >= 3) {
            return "今日验证码获取上限";
        }

        // 4.判断该手机号码是否存在未过期的验证码
        Boolean flag = redisTemplate.hasKey(codeStr);
        if (flag) {
            return "请不要频繁获取验证码";
        }

        // 5.调用模板类发送短信验证码同时将验证码存到redis中保存5分钟

        // 随机生成一个6位验证码UUID
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", mobile);
        querys.put("param", "code:" + uuid);
        querys.put("tpl_id", "TP1711063");
        //Boolean smsFlag = smsTemplate.sendSms(querys);
        //if(!smsFlag) { return "发送验证码失败"; }

        // 验证码存储到redis中保存5分钟
        redisTemplate.opsForValue().set(codeStr, uuid, 5, TimeUnit.MINUTES);

        // 将获取验证码的数量设置到redis中
        Long expire = redisTemplate.getExpire(codeContStr, TimeUnit.MINUTES);
        if (expire == null || expire <= 0) {
            expire = (long) (24 * 60);
        }

        count++;
        redisTemplate.opsForValue().set(codeContStr, count + "", expire, TimeUnit.MINUTES);

        return "验证码发送成功";

    }
}
