package com.atguigu.gmall.sms.feign;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.vo.SaleVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


public interface GmallSmsApi {
    @PostMapping("sms/skubounds/save")
    public Resp<Object> save(@RequestBody SaleVO saleVO);
}
