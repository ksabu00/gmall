package com.atguigu.gmall.order.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.vo.UserInfo;
import com.atguigu.gmall.cart.vo.CartItemVO;
import com.atguigu.gmall.order.Interceptor.LoginInterceptor;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.order.vo.OrderItemVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallUmsClient gmallUmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private GmallCartClient gmallCartClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public OrderConfirmVO confirm() {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        // 获取用户信息
        UserInfo userInfo = LoginInterceptor.get();
        Long userId = userInfo.getUserId();
        // 获取用户的收获地址列表
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> addressResp = this.gmallUmsClient.queryAddressByUserId(userId);
            orderConfirmVO.setAddress(addressResp.getData());
        }, threadPoolExecutor);

        // 获取购物车中的选中记录
        CompletableFuture<Void> cartFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<CartItemVO>> listResp = this.gmallCartClient.queryCartItemVO(userId);
            List<CartItemVO> itemVOS = listResp.getData();
            return itemVOS;
        }, threadPoolExecutor).thenAcceptAsync(itemVOS -> {
            if (CollectionUtils.isEmpty(itemVOS)) {
                return;
            }
            // 把购物车选中记录转换成订货清单
            List<OrderItemVO> orderItems = itemVOS.stream().map(cartItemVO -> {
                OrderItemVO orderItemVO = new OrderItemVO();
                // 根据skuId查询sku
                Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(cartItemVO.getSkuId());
                SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                // 根据skuId查询销售属性
                Resp<List<SkuSaleAttrValueEntity>> skuSaleResp = this.gmallPmsClient.querySaleAttrBySkuId(cartItemVO.getSkuId());
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleResp.getData();
                Resp<List<ItemSaleVO>> itemVOs = this.gmallSmsClient.querySaleItemVOs(cartItemVO.getSkuId());

                orderItemVO.setSkuAttrValue(skuSaleAttrValueEntities);
                orderItemVO.setTitle(skuInfoEntity.getSkuTitle());
                orderItemVO.setSkuId(cartItemVO.getSkuId());
                orderItemVO.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                orderItemVO.setCount(cartItemVO.getCount());
                // 根据skuId查询营销信息
                Resp<List<ItemSaleVO>> saleResp = this.gmallSmsClient.querySaleItemVOs(cartItemVO.getSkuId());
                List<ItemSaleVO> itemSaleVOS = saleResp.getData();
                orderItemVO.setSales(itemSaleVOS);
                // 根据skuId查询库存信息
                Resp<List<WareSkuEntity>> storeResp = this.gmallWmsClient.queryWareSkuBySkuId(cartItemVO.getSkuId());
                List<WareSkuEntity> wareSkuEntities = storeResp.getData();
                orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));

                orderItemVO.setWeight(skuInfoEntity.getWeight());

                return orderItemVO;
            }).collect(Collectors.toList());
            orderConfirmVO.setOrderItems(orderItems);
        }, threadPoolExecutor);

        CompletableFuture<Void> boundFuture = CompletableFuture.runAsync(() -> {
            // 获取用户信息(积分)
            Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryUserById(userInfo.getUserId());
            MemberEntity memberEntity = memberEntityResp.getData();
            orderConfirmVO.setBounds(memberEntity.getIntegration());
        }, threadPoolExecutor);

        CompletableFuture<Void> idFuture = CompletableFuture.runAsync(() -> {
            // 生成唯一标志，防止重复提交
            String timeId = IdWorker.getTimeId();
            orderConfirmVO.setOrderToken(timeId);
        }, threadPoolExecutor);

        CompletableFuture.allOf(addressFuture, cartFuture, boundFuture, idFuture).join();

        return orderConfirmVO;
    }
}
