package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * sku销售属性&值
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-02 21:54:09
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageVo queryPage(QueryCondition params);
}

