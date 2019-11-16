package com.atguigu.gmall.cart.api.feign;

import com.atguigu.gmall.sms.feign.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
