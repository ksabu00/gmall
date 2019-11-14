package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.vo.Cart;

import java.util.List;

public interface CartService {

    public void addCart(Cart cart);

    List<Cart> queryCarts();

    void updateCart(Cart cart);

    void deleteCart(Long skuId);

    void checkCart(List<Cart> carts);
}
