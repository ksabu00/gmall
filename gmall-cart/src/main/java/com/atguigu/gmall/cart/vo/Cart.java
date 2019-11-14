package com.atguigu.gmall.cart.vo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {
    private Long skuId;// 商品id

    private Integer count;// 购买数量

    private BigDecimal price;// 购买单价

    private Boolean check;// 是否选中

    private String defaultImage;// 订单页默认图片

    private String title;// 标题

    private List<SkuSaleAttrValueEntity> skuSaleAttrValue;// 商品规格参数

    private List<ItemSaleVO> sales;// 销售属性
}
