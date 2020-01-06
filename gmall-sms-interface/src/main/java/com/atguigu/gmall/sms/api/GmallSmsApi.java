package com.atguigu.gmall.sms.api;

import com.atguigu.core.bean.Resp;

import com.atguigu.gmall.sms.vo.SaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sale")
    public Resp<Object> saveSales(@RequestBody SaleVo saleVo);
}


