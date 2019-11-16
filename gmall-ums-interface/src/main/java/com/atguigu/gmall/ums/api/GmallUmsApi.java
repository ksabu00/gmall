package com.atguigu.gmall.ums.api;


import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GmallUmsApi {
    @ApiOperation("详情查询")
    @GetMapping("ums/member/info/{id}")
    public Resp<MemberEntity> queryUserById(@PathVariable("id") Long id);
    /**
     * 根据id来查询积分
     * @param id
     * @return
     */
    @GetMapping("ums/member/info/{id}")
    public Resp<MemberEntity> info(@PathVariable("id") Long id);

    /**
     * 根据userId查询收货地址
     */
    @GetMapping("ums/memberreceiveaddress/{userId}")
    public Resp<List<MemberReceiveAddressEntity>> queryAddressByUserId(@PathVariable("userId")Long userId);

    @ApiOperation("用户登录查询登录信息接口")
    @GetMapping("ums/member/query")
    public Resp<MemberEntity> queryUser(@RequestParam("username")String username, @RequestParam("password")String password);
}
