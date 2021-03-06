package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.vo.GroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;

import javax.validation.OverridesAttribute;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidPage(Long cid, QueryCondition condition) {

        /**
         *
         */
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(condition), new QueryWrapper<AttrGroupEntity>().eq("catelog_id",cid));

        //IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(condition), new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        return new PageVo(page);
    }

    @Override
    public AttrGroupVO queryById(Long gid) {
        AttrGroupVO attrGroupVO = new AttrGroupVO();
        // 先查分组
        AttrGroupEntity attrGroupEntity = this.attrGroupDao.selectById(gid);
        BeanUtils.copyProperties(attrGroupEntity, attrGroupVO);

        // 查询分组下的关联关系
        List<AttrAttrgroupRelationEntity> relations = this.relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_group_id", gid));

        // 判断关联关系是否为空，如果为空，直接返回
        if (CollectionUtils.isEmpty(relations)){
            return attrGroupVO;
        }
        attrGroupVO.setRelations(relations);

        // 收集分组下的所有规格id
        List<Long> attrIds = relations.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());
        List<AttrEntity> attrEntities = this.attrDao.selectBatchIds(attrIds);
        attrGroupVO.setAttrEntities(attrEntities);

        return attrGroupVO;
    }

    @Override
    public List<AttrGroupVO> queryByCid(Long cid) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));

        List<AttrGroupVO> attrGroupVOS = attrGroupEntities.stream().map(attrGroupEntity -> {
            return this.queryById(attrGroupEntity.getAttrGroupId());
        }).collect(Collectors.toList());

        return attrGroupVOS;
    }

    @Override
    public List<GroupVO> queryGroupVOByCid(Long cid , Long spuId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        if (CollectionUtils.isEmpty(attrGroupEntities)){
            return null;
        }
        attrGroupEntities.stream().map(attrGroupEntity -> {
            GroupVO groupVO = new GroupVO();
            List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueDao.queryByCidAndSpuId(spuId, attrGroupEntity.getAttrGroupId());
            groupVO.setBaseAttrValues(productAttrValueEntities);
            return groupVO;
        }).collect(Collectors.toList());
        return null;
    }

}