package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import lombok.Data;

import java.util.List;

@Data
public class ItemVO extends SkuInfoEntity {
    private SpuInfoEntity spuInfo;// 商品基本信息

    private BrandEntity brand;// 品牌

    private CategoryEntity category;// 排序

    private List<String> pics;// 详情图品

    private List<ItemSaleVO> sales;// 满减信息

    private Boolean store;// 库存是否有货

    private List<SkuSaleAttrValueEntity> skuSales;// 销售属性

    private SpuInfoDescEntity desc;// 商品描述

    private List<GroupVO> groups;// 组及组下的规格属性及值

}
