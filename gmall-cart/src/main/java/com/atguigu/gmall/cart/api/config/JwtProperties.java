package com.atguigu.gmall.cart.api.config;


import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "cart.jwt")
public class JwtProperties {

    private String pubKeyPath;// 公钥

    private PublicKey publicKey;// 公钥

    private String cookieName;//

    private String userKeyName;// 用户名

    private Integer expire;// 过期时间

    @PostConstruct
    public void init(){
        try {
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("初始化公私钥失败");
            e.printStackTrace();
        }
    }
}
