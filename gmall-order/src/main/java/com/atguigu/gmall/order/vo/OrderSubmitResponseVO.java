package com.atguigu.gmall.order.vo;

import lombok.Data;

@Data
public class OrderSubmitResponseVO {
    private String orderId;// 订单编号

    private Integer code;// 1-不可重复提交或页面已过期 2-库存不足 3-价格校验不合法 等
}