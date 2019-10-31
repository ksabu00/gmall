package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;

@Data
public class SpuInfoVO extends SpuInfoEntity {
    /**
     * spuInfo扩展对象
     * 包含：spuInfo基本信息、spuImages图片信息、baseAttrs基本属性信息、skus信息
     */
    // 商品图像信息
    private List<String> spuImages;

    // 商品基本属性信息
    private List<ProductAttrValueVO> baseAttrs;

    // skus信息
    private List<SkuInfoVO> skus;

}
