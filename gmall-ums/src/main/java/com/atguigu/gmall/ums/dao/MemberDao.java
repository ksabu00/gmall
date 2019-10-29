package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author ksabu00
 * @email 2054693@qq.com
 * @date 2019-10-28 20:34:53
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
