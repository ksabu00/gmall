package com.atguigu.gmall.ums.api;


import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.ums.entity.MemberEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface GmallUmsApi {

    @ApiOperation("用户登录查询登录信息接口")
    @GetMapping("ums/member/query")
    public Resp<MemberEntity> queryUser(@RequestParam("username")String username, @RequestParam("password")String password);
}
