package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsServiceFeign;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.BaseAttrValueVo;
import com.atguigu.gmall.pms.vo.SkuInfoVo;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
//import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.sms.vo.SaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDao;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDao spuInfoDao;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuByCidOrKey(QueryCondition queryCondition, Long catId) {
        //根据key和cid查询所有的spu信息
        //先判断是查全站还是分类
        //如果是全站就不用需要catId，不用拼接了
        //如果不是全站（就是分类），就需要查询catId,catId有相应的值，做拼接和条件
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper();
        if (catId != 0) {
            wrapper.eq("catalog_id", catId);
        }
        //不管是否查询全站还是分类都需要传key
        String key = queryCondition.getKey();
        //判断key是否为空，为空就不做判断
        if (StringUtils.isNotBlank(key)) {
            //不为空需要拼接key的值
            wrapper.and(t -> t.eq("id", key).or().like("spu_name", key));
        }
        IPage<SpuInfoEntity> spuInfoEntityIPage = this.page(new Query<SpuInfoEntity>().getPage(queryCondition), wrapper);
        return new PageVo(spuInfoEntityIPage);
    }

    @Override
    @GlobalTransactional
    public void bigSave(SpuInfoVo spuInfoVo) {

        //保存spu的相关信息
        //1.1spuInfo
        Long spuId = saveSpuInfo(spuInfoVo);

        //1,2spuInfoDesc就是图片描述
        spuInfoDescService.saveSpuDesc(spuInfoVo, spuId);
        //1.3基础属性的相关信息
        productAttrValueService.saveBaseAttr(spuInfoVo, spuId);
        //保存sku的相关属性
        skuInfoService.saveSku(spuInfoVo, spuId);


    }
    public Long saveSpuInfo(SpuInfoVo spuInfoVo) {
        Long cid = spuInfoVo.getCatalogId();
        spuInfoVo.setCreateTime(new Date());
        spuInfoVo.setUodateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        return spuInfoVo.getId();
    }
}
