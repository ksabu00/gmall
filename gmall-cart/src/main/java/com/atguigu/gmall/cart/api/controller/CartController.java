package com.atguigu.gmall.cart.api.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.api.service.CartService;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.cart.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {

    /*@GetMapping
    public UserInfo test(HttpServletRequest request){
        // 拦截器提供的静态方法，返回UserInfo
        return LoginInterceptor.get();
    }*/


    @Autowired
    private CartService cartService;



    @GetMapping("order/{userId}")
    public Resp<List<CartItemVO>> queryCartItemVO(@PathVariable("userId")Long userId){
        List<CartItemVO> itemVOList = this.cartService.queryCartItemVO(userId);
        return Resp.ok(itemVOList);
    }


    @PostMapping("check")
    public Resp<Object> checkCart(@RequestBody List<Cart> carts){
        this.cartService.checkCart(carts);

        return Resp.ok(null);
    }

    @PostMapping("{skuId}")
    public Resp<Object> deleteCart(@PathVariable("skuId")Long skuId){
        this.cartService.deleteCart(skuId);

        return Resp.ok(null);
    }

    @PostMapping("update")
    public Resp<Object> updateCart(@RequestBody Cart cart){
        this.cartService.updateCart(cart);

        return Resp.ok(null);
    }

    @GetMapping
    public Resp<List<Cart>> queryCarts(){
        List<Cart> carts = this.cartService.queryCarts();
        return Resp.ok(carts);
    }

    @PostMapping
    public Resp<Object> addCart(@RequestBody Cart cart){
        this.cartService.addCart(cart);

        return Resp.ok(null);
    }
}
