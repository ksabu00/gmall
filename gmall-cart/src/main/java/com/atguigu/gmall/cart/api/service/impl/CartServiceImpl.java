package com.atguigu.gmall.cart.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.vo.UserInfo;
import com.atguigu.gmall.cart.api.Interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.api.feign.GmallPmsClient;
import com.atguigu.gmall.cart.api.feign.GmallSmsClient;
import com.atguigu.gmall.cart.api.service.CartService;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.cart.vo.CartItemVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final String CART_PREFIX = "cart:key";

    private static final String CURRENT_PRICE_PREFIX = "cart:price";

    @Override
    public void addCart(Cart cart) {
        String key = getKey();

        Integer count = cart.getCount();
        Long skuId = cart.getSkuId();

        // 购物车是否存在该skuId的商品
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        // 存在则在原来的数量上加
        if (hashOps.hasKey(cart.getSkuId().toString())){
            // 注意这里的skuId要转化成String，因为redis中保存的都是String
            String cartJson = hashOps.get(skuId.toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount() + count);
            //System.out.println(cart.getCount());
        }else {
            // 不存在则新增
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();

            cart.setTitle(skuInfoEntity.getSkuTitle());
            cart.setCount(count);
            cart.setCheck(false);
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setSales(this.gmallSmsClient.querySaleItemVOs(skuId).getData());
            cart.setSkuSaleAttrValue(this.gmallPmsClient.querySaleAttrBySkuId(skuId).getData());
            this.redisTemplate.opsForValue().set(CURRENT_PRICE_PREFIX + skuId, skuInfoEntity.getPrice().toString());
        }
        // 同步到redis
        hashOps.put(skuId.toString(), JSON.toJSONString(cart));
    }

    public String getKey() {
        String key = CART_PREFIX;
        // 判断状态
        UserInfo userInfo = LoginInterceptor.get();
        // 登录则使用userId作为key
        if (userInfo.getUserId() != null){
            key += userInfo.getUserId();
        }else {
            // 未登录则使用userKey作为key
            key += userInfo.getUserKey();
        }
        return key;
    }

    @Override
    public List<Cart> queryCarts() {
        // 获取未登录状态的购物车
        UserInfo userInfo = LoginInterceptor.get();
        String key1 = CART_PREFIX + userInfo.getUserKey();

        BoundHashOperations<String, Object, Object> userKeyOps = this.redisTemplate.boundHashOps(key1);
        List<Object> cartJsonList = userKeyOps.values();
        // 存在则反序列化
        List<Cart> userKeyCarts = null;
        if (!CollectionUtils.isEmpty(cartJsonList)){
            userKeyCarts = cartJsonList.stream().map(cartJson ->{
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }
        // 判断登录状态
        if (userInfo.getUserId() == null){
            // 未登录直接返回
            return userKeyCarts;
        }
        // 登录，查询登录状体的购物车
        String key2 = CART_PREFIX + userInfo.getUserId();
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key2);
        // 判断未登录的购物车是否为空
        if(!CollectionUtils.isEmpty(cartJsonList)){
            // 不为空，合并购物车
            userKeyCarts.forEach(cart -> {
            if (userIdOps.hasKey(cart.getSkuId().toString())){
                // 注意这里的skuId要转化成String，因为redis中保存的都是String
                String cartJson = userIdOps.get(cart.toString()).toString();
                Cart cart1 = JSON.parseObject(cartJson, Cart.class);
                cart.setCount(cart1.getCount() + cart.getCount());
                //System.out.println(cart.getCount());
            }else {
                // 不存在则新增
                userIdOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
                }
            });
            this.redisTemplate.delete(key1);
        }
        // 为空直接返回登录状态的购物车
        List<Object> userIdJsonList = userIdOps.values();
        if (CollectionUtils.isEmpty(userIdJsonList)){
            return null;
        }
        return userIdJsonList.stream().map(userIdCartJson -> {
            Cart cart = JSON.parseObject(userIdCartJson.toString(), Cart.class);
            cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PREFIX + cart.getSkuId())));
            return cart;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateCart(Cart cart) {
        // 不管状态，直接获取key
        String key = getKey();

        Integer count = cart.getCount();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        if (hashOps.hasKey(cart.getSkuId().toString())){
            // 获取购物车中的更新数量的购物记录
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }
    }

    @Override
    public void deleteCart(Long skuId) {
        // 不管状态，直接获取key
        String key = getKey();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        if (hashOps.hasKey(skuId.toString())){
            hashOps.delete(skuId.toString());
        }
    }

    @Override
    public void checkCart(List<Cart> carts) {
        String key = getKey();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        carts.forEach(cart -> {
            Boolean check = cart.getCheck();
            if (hashOps.hasKey(cart.getSkuId().toString())){
                // 获取购物车中的更新数量的购物记录
                String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
                cart = JSON.parseObject(cartJson, Cart.class);
                cart.setCheck(check);
                hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            }
        });
    }

    @Override
    public List<CartItemVO> queryCartItemVO(Long userId) {
        // 登录，查询登录状体的购物车
        String key2 = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key2);

        // 为空直接返回登录状态的购物车
        List<Object> userIdJsonList = userIdOps.values();
        if (CollectionUtils.isEmpty(userIdJsonList)){
            return null;
        }
         return userIdJsonList.stream().map(userIdCartJson -> {
                Cart cart = JSON.parseObject(userIdCartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PREFIX + cart.getSkuId())));
                return cart;
        }).filter(cart -> cart.getCheck()).map(cart -> {
             CartItemVO cartItemVO = new CartItemVO();
             cartItemVO.setSkuId(cart.getSkuId());
             cartItemVO.setCount(cart.getCount());
             return cartItemVO;
         }).collect(Collectors.toList());
        //return null;
    }
}
