package com.atguigu.gmall.cart.api.Interceptor;

import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.vo.UserInfo;
import com.atguigu.gmall.cart.api.config.JwtProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties({JwtProperties.class})
public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private JwtProperties jwtProperties;

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // threadLocal的载客信息，userId , userKey
        UserInfo userInfo = new UserInfo();

        // 获取cookie信息(GMALL_TOKEN, UserKey)
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());

        String userKey = CookieUtils.getCookieValue(request, this.jwtProperties.getUserKeyName());

        // 如果userKey为空，生成一个userKey设置进去
        if (StringUtils.isBlank(userKey)){
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, this.jwtProperties.getUserKeyName(), userKey, this.jwtProperties.getExpire());
        }
        // 同时将userKey设置到userInfo作为载客信息
        userInfo.setUserKey(userKey);
        if(StringUtils.isEmpty(token)){
            THREAD_LOCAL.set(userInfo);
            return true;
        }


        try {
            // 解析gmall_token
            Map<String, Object> userInfoMap = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());

            userInfo.setUserId(Long.valueOf(userInfoMap.get("id").toString()));

        }catch (Exception e){
            e.printStackTrace();
        }
        THREAD_LOCAL.set(userInfo);
        return true;
    }
    // 由于调用线程池，需要关闭连接，防止内存线程占用
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }

    /**
     * 提供给外部调用，返回UserInfo登录信息
     * @return
     */
    public static UserInfo get(){

        return THREAD_LOCAL.get();
    }
}
