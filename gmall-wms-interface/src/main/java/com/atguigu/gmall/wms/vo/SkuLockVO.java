package com.atguigu.gmall.wms.vo;

import lombok.Data;

@Data
public class SkuLockVO {
    private Long skuId;// 锁库存的skuId

    private Integer count;// 需要锁的件数

    private Long wareSkuId;// wms_ware_sku表的主键

    private String orderToken;// 订单编号

    private Boolean lock;// 是否锁住
}
