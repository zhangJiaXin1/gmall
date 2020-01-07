package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.vo.BaseAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageVo(page);
    }
    @Transactional
    public void saveBaseAttr(SpuInfoVo spuInfoVo, Long spuId) {
        List<BaseAttrValueVo> baseAttrs = spuInfoVo.getBaseAttrs();
        //此处不能用foreach因为有的属性没有，需要临时添加
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            //接收一个函数作为参数，该函数会被应用到每个元素上，并将其映射成一个新的元素。
            List<ProductAttrValueEntity> collect = baseAttrs.stream().map(baseAttrValueVo -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                BeanUtils.copyProperties(baseAttrValueVo, productAttrValueEntity);
                productAttrValueEntity.setAttrSort(0);
                productAttrValueEntity.setSpuId(spuId);
                productAttrValueEntity.setQuickShow(0);
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(collect);
        }
    }
}