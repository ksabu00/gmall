package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVO {
    private String orderToken;// 防止重复提交，订单编号

    private List<MemberReceiveAddressEntity> address;// 收货地址

    private Integer payType;// 支付方式

    private String deliveryCompany;// 配送公司（配送方式）

    private List<OrderItemVO> orderItemVOS;// 订单详情

    private Integer useIntegration;// 下单时使用的积分

    private BigDecimal totalPrice;// 总价，用于验证

}
