package com.auguigu.gmall.item.controller;

import com.atguigu.core.bean.Resp;
import com.auguigu.gmall.item.service.ItemService;
import com.auguigu.gmall.item.vo.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("{skuId}")
    public Resp<ItemVO> item(@PathVariable("skuId")Long skuId){

        ItemVO itemVO = this.itemService.item(skuId);

        return Resp.ok(itemVO);
    }
}
