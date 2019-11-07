package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.sms.vo.SaleVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescDao spuInfoDescDao;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesDao skuImagesDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private GmallSmsClient smsClient;

    @Override
    @GlobalTransactional
    public void saveSpuInfoVO(SpuInfoVO spuInfoVo) {
        //// 1：保存spu相关信息，获取spu_id

        // 1.1 保存spu基本信息获取spu_info
        Long spuId = saveSpuInfo(spuInfoVo);

        // 1.2 保存spu的描述信息 spu_info_desc
        saveSpuDesc(spuInfoVo, spuId);

        // 1.3 保存spu的规格参数信息
        saveBaseAttrs(spuInfoVo, spuId);

        // 2：保存sku相关信息
        // 3 保存营销相关信息，需要远程调用gmall-sms
        // 3.1 积分优惠
        // 3.2 满减优惠
        // 3.3 数量折扣
        saveSkuInfoWithSaleInfo(spuInfoVo, spuId);

        // 最后制造异常
        //int i = 1 / 0;
    }

    public void saveSkuInfoWithSaleInfo(SpuInfoVO spuInfoVo, Long spuId) {

        List<SkuInfoVO> skuInfoVOs = spuInfoVo.getSkus();
        // 如果sku信息不为空则继续
        if (CollectionUtils.isEmpty(skuInfoVOs)){
            return;
        }
        // 2.1 保存sku基本信息
        skuInfoVOs.forEach(skuInfoVO -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(skuInfoVO, skuInfoEntity);
            // 品类和分类的id需要从spuInfo中获取
            skuInfoEntity.setBrandId(spuInfoVo.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoVo.getCatalogId());
            // 获取随机的uuid作为sku的编码
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            // 获取图片列表
            List<String> images = skuInfoVO.getImages();
            // 如果图片不是null，则设置为默认图片
            if (!CollectionUtils.isEmpty(images)){
                // 设置第一张图片为默认图片
                skuInfoEntity.setSkuDefaultImg(skuInfoEntity.getSkuDefaultImg() == null ? images.get(0) : skuInfoEntity.getSkuDefaultImg());
            }
            skuInfoEntity.setSpuId(spuId);
            this.skuInfoDao.insert(skuInfoEntity);
            // 获取skuId
            Long skuId = skuInfoEntity.getSkuId();

        // 2.2 保存sku图片信息
            if (!CollectionUtils.isEmpty(images)) {
                images.forEach(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(StringUtils.equals(image, skuInfoEntity.getSkuDefaultImg()) ? 1 : 0);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setImgUrl(image);
                    this.skuImagesDao.insert(skuImagesEntity);
                });
            }
            if (!CollectionUtils.isEmpty(images)){
                String defaultImage = images.get(0);
                List<SkuImagesEntity> skuImageses = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setDefaultImg(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setImgUrl(image);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImageses);
            }
        // 2.3 保存sku的规格参数(销售属性)
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            saleAttrs.forEach(saleAttr -> {
                //saleAttr.setAttrName(this.attrDao.selectById(saleAttr.getId()).getAttrName());
                    saleAttr.setAttrSort(0);
                    saleAttr.setSkuId(skuId);
            });
            this.skuSaleAttrValueService.saveBatch(saleAttrs);
        // 3 保存营销相关信息，需要远程调用gmall-sms
            // 3.1 积分优惠
            // 3.2 满减优惠
            // 3.3 数量折扣
            SaleVO saleVO = new SaleVO();
            BeanUtils.copyProperties(skuInfoVO, saleVO);

            this.smsClient.saveSale(saleVO);
        });
    }

    public void saveBaseAttrs(SpuInfoVO spuInfoVo, Long spuId) {
        List<ProductAttrValueVO> baseAttrs = spuInfoVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){
            List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(productAttrValueVO -> {
                productAttrValueVO.setSpuId(spuId);
                productAttrValueVO.setAttrSort(0);
                productAttrValueVO.setQuickShow(0);
                return productAttrValueVO;
            }).collect(Collectors.toList());
            this.productAttrValueService.saveBatch(productAttrValueEntities);
        }
    }

    public void saveSpuDesc(SpuInfoVO spuInfoVo, Long spuId) {
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        // 注意：spu_info_desc的主键时spu_id；需要在实体类中设置
        spuInfoDescEntity.setSpuId(spuId);
        // 将图片描述，保存到spu详情中，图片地址以逗号分割
        spuInfoDescEntity.setDecript(StringUtils.join(spuInfoVo.getSpuImages(), ","));
        this.spuInfoDescDao.insert(spuInfoDescEntity);
    }

    public Long saveSpuInfo(SpuInfoVO spuInfoVo) {
        spuInfoVo.setPublishStatus(1);// 默认为已上架
        spuInfoVo.setCreateTime(new Date());
        spuInfoVo.setUodateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        return spuInfoVo.getId();
    }

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuByCatId(QueryCondition condition, Long catId) {
        // 封装分页条件
        IPage<SpuInfoEntity> page = new Query<SpuInfoEntity>().getPage(condition);
        // 建立查询条件
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        // 如果分类id不为0，要根据id查询，否则查全部
        if (catId != 0) {
            wrapper.eq("catelog_id", catId);
        }
        // 如果用户输入了检索条件，根据检索条件查询
        String key = condition.getKey();
        if (StringUtils.isNotBlank(key)){
            wrapper.and(t -> t.like("spu_name", key).or().like("id", key));
        }

        return new PageVo(this.page(page,wrapper));
    }


}