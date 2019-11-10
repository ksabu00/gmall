package com.atguigu.gmall.sms.feign;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.sms.vo.SaleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


public interface GmallSmsApi {

    @GetMapping("sms/skubounds/item/sales/{skuId}")
    public Resp<List<ItemSaleVO>> querySaleItemVOs(@PathVariable("skuId")Long skuId);

    @PostMapping("sms/skubounds/save")
    public Resp<Object> saveSale(@RequestBody SaleVO saleVO);
}
