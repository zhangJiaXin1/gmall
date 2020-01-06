package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsServiceFeign;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.pms.vo.BaseAttrValueVo;
import com.atguigu.gmall.pms.vo.SkuInfoVo;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
//import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.sms.vo.SaleVo;
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
import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private GmallSmsServiceFeign gmallSmsServiceFeign;
    @Autowired
    private SpuInfoDao spuInfoDao;
    @Autowired
    private SpuInfoDescDao spuInfoDescDao;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesService skuImagesService;
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
    public void bigSave(SpuInfoVo spuInfoVo) {

        //保存spu的相关信息
        //1.1spuInfo
       SpuInfoEntity spuInfoEntity=new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoVo, spuInfoEntity);
        Long cid = spuInfoVo.getCatalogId();

        spuInfoVo.setCreateTime(new Date());
        spuInfoEntity.setUodateTime(spuInfoVo.getCreateTime());
        spuInfoVo.setUodateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        Long spuId = spuInfoVo.getId();

        //1,2spuInfoDesc就是图片描述
        List<String> spuImages = spuInfoVo.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)) {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setDecript(StringUtils.join(spuImages, ","));
            spuInfoDescEntity.setSpuId(spuId);
            spuInfoDescDao.insert(spuInfoDescEntity);
        }
        //1.3基础属性的相关信息
        List<BaseAttrValueVo> baseAttrs = spuInfoVo.getBaseAttrs();
        //此处不能用foreach因为有的属性没有，需要临时添加
        if (!CollectionUtils.isEmpty(baseAttrs)) {
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
        //保存sku的相关属性
        List<SkuInfoVo> skus = spuInfoVo.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        //2.1skuInfo
        skus.forEach(skuInfoVo -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(skuInfoVo, skuInfoEntity);
            List<String> images = skuInfoVo.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                skuInfoEntity.setSkuDefaultImg(skuInfoEntity.getSkuDefaultImg() == null ? images.get(0) : skuInfoEntity.getSkuDefaultImg());
            }
            skuInfoEntity.setSpuId(spuId);
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString());
            skuInfoEntity.setCatalogId(spuInfoVo.getCatalogId());
            skuInfoEntity.setBrandId(spuInfoVo.getBrandId());
            this.skuInfoDao.insert(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();
            //2.2skuInfoImages
            if (!CollectionUtils.isEmpty(images)) {
                List<SkuImagesEntity> collect = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setDefaultImg(StringUtils.equals(image, skuInfoEntity.getSkuDefaultImg()) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(collect);
            }
            //2.3skuSaleAttrValue
            //遍历的时候缺什么给他补什么,然后会添加代集合中
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                saleAttrs.forEach(saleAttr -> {
                    saleAttr.setSkuId(skuId);
                    saleAttr.setAttrSort(0);
                });
                skuSaleAttrValueService.saveBatch(saleAttrs);
            }
            //保存营销的相关信息

            //3.1skuBounds积分
            //3.2skuladder打折
            //3.3shuFullReductoion满减
            SaleVo saleVo = new SaleVo();
            BeanUtils.copyProperties(skuInfoVo,saleVo );
            saleVo.setSkuId(skuId);
            gmallSmsServiceFeign.saveSales(saleVo);
        });


    }
}
