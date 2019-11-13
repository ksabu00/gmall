package com.atguigu.gmall.gateway.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.asymmetric.RSA;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties("auth.jwt")
public class JwtProperties {
    /*
    auth:
    jwt:
    publickey: C:\\temp\\rsa\\ras.pub
    privatekey: C:\\temp\\rsa\\ras.pri
    expire: 30 # 单位秒
    cookieName: GMALL_TOKEN
    */
    private String publicKeyPath;

    private String privateKeyPath;

    private Integer expire;

    private String cookieName;

    private PublicKey publicKey;

    private PrivateKey privateKey;

    private String secret;


    @PostConstruct
    public void init(){
        try {
            // 初始化公私钥文件
//            File publicFile = new File(publicKeyPath);
//            File privateFile = new File(privateKeyPath);
            // 检查文件是否为空
//            if (!publicFile.exists() || !privateFile.exists()){
////                RsaUtils.generateKey(publicKeyPath, privateKeyPath, secret);
////            }
            // 读取公私钥文件
            publicKey = RsaUtils.getPublicKey(publicKeyPath);
            //privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        }catch (Exception e){
            log.error("初始化公私钥失败！");
            e.printStackTrace();
        }finally {

        }
    }
}

