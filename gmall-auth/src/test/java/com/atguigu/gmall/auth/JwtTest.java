package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class JwtTest {
    private static final String pubKeyPath = "C:\\temp\\rsa\\rsa.pub";

    private static final String priKeyPath = "C:\\temp\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    /*@Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }*/

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "ksabu");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJrc2FidSIsImV4cCI6MTU3MzU3NzUzN30.foeS2VFzT9zznno26SltD4tCVuahbxck7UxDO25LJjpg2SPmTDcRuOEnpU6OknFdhgMqbxIyLt_a7gR0_4If4yyGEbU3O9lAtdgXU3o-VMFW0Eo1DvYaZMwXDmTCUSvU_NeSp2JPCZ9LQteMo9isSVzDJvVGmwICVS1By7_JjYMdI7kC-UJOh2-Jy7ei90lqtRvcsBSWAztyAQ6NGCSKSX7qI6EwacOd5VQvMXUukMSZ114efKli-uA-d_tzO1Sxb-_n57Z5VeN-VSOFkeeXIA27YMmH2isn0RvbLlUF5_gELvxRwfIZFpi8vIQJPkV4jT_n0BlQ4-jk8C9g1blfWA";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
