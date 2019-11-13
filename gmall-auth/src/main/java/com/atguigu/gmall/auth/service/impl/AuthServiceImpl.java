package com.atguigu.gmall.auth.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.ums.entity.MemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties({JwtProperties.class})
public class AuthServiceImpl implements AuthService {
    @Autowired
    private GmallUmsClient gmallUmsClient;
    @Autowired
    private JwtProperties jwtProperties;
    @Override
    public String accredit(String username, String password) {
        // 1:调用用户中心的登录接口，查询用户信息
        Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryUser(username, password);
        MemberEntity memberEntity = memberEntityResp.getData();
        // 2：是否查询到，查询不到则直接返回用户信息
        if (memberEntity == null) {
            return null;
        }
        // 3：存在则直接返回生成JWT
        Map<String, Object> map = new HashMap<>();
        map.put("id", memberEntity.getId());
        map.put("username", memberEntity.getUsername());
        try {
            return JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4：生成的JWT放入cookie中
        return null;
    }
}
