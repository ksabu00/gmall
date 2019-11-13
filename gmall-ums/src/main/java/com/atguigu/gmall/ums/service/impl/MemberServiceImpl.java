package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        // 建立一个查询条件
        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<>();
        // 判断输入的type
        //1，用户名；2，手机；3，邮箱
        switch (type){
            case 1: wrapper.eq("username", data);break;
            case 2: wrapper.eq("mobile", data);break;
            case 3: wrapper.eq("email", data);break;
            default: return false;
        }
        // 当查询到的数为0，那么代表可以注册
        return this.count(wrapper) == 0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) {
        // 校验验证码
        System.out.println("当前验证码为:" + code);

        // 加盐
        String salt = UUID.randomUUID().toString().substring(0, 6);
        // 加密
        memberEntity.setPassword(DigestUtils.md5Hex(DigestUtils.md5Hex(memberEntity.getPassword()) + salt));
        // 注册
        memberEntity.setCreateTime(new Date());
        memberEntity.setStatus(1);
        memberEntity.setSalt(salt);
        memberEntity.setIntegration(0);
        memberEntity.setGrowth(0);

        boolean b = this.save(memberEntity);
        // 删除redis中的验证码

    }

    @Override
    public MemberEntity queryUser(String username, String password) {
        // 查询用户名是否存在
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", username));
        // 校验用户名
        if (memberEntity == null){
            throw new IllegalArgumentException("用户名不合法！");
        }
        // 获取盐
        String salt = memberEntity.getSalt();
        // 校验密码
        if (!memberEntity.getPassword().equals(DigestUtils.md5Hex(DigestUtils.md5Hex(password) + salt))){
            throw new IllegalArgumentException("密码不合法！");
        }
        return memberEntity;
    }
}