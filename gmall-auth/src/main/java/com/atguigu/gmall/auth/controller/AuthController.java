package com.atguigu.gmall.auth.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/accredit")
    public Resp<Object> accredit(@RequestParam("username")String username, @RequestParam("password")String password, HttpServletRequest request, HttpServletResponse response){
        String jwtToken = this.authService.accredit(username, password);
        System.out.println(jwtToken);
        if (StringUtils.isBlank(jwtToken)){
            return Resp.fail("失败");
        }

        // 4：把生成的放入cookie中
        CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), jwtToken, jwtProperties.getExpire() * 60);
        return Resp.ok(null);
    }
}
