package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * spu属性值
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-02 21:54:09
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageVo queryPage(QueryCondition params);

    void saveBaseAttr(SpuInfoVo spuInfoVo, Long spuId);

    List<ProductAttrValueEntity> querySearchAttrBySpuId(Long spuId);
}

