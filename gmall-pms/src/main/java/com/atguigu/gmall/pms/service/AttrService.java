package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.AttrVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品属性
 *
 * @author ksabu00
 * @email 2054693@qq.com
 * @date 2019-10-28 20:29:35
 */
public interface AttrService extends IService<AttrEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryByCidTypePage(QueryCondition condition, Long cid, Integer type);

    void saveAttrVO(AttrVO attrVO);
}

