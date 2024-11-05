package com.rafa.order_admin_service.feign;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient()//加入要映射的名稱（全大寫）
public interface OrderInterface {
    //加入要映射的方法
}
