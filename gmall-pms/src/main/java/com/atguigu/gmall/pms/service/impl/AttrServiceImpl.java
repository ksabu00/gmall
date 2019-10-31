package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.vo.AttrVO;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    private AttrDao attrDao;

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidTypePage(QueryCondition condition, Long cid, Integer type) {
        // cid为必要参数
        // 创建查询条件
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id", cid);
        // 0-销售属性，1-基本属性
        if (type != null) {
            wrapper.eq("attr_type", type);
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(condition), wrapper);
        return new PageVo(page);
    }

    @Override
    public void saveAttrVO(AttrVO attrVO) {
        // 新增规格参数
        this.attrDao.insert(attrVO);

        // 增加中间表并设置相应的值
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrGroupId(attrVO.getAttrGroupId());
        relationEntity.setAttrId(attrVO.getAttrId());
        relationDao.insert(relationEntity);
    }
}