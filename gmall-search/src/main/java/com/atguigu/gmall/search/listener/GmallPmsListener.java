package com.atguigu.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsFeign;
import com.atguigu.gmall.search.feign.GmallWmsFeign;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GmallPmsListener {
    @Autowired
    private GmallWmsFeign wmsFeign;
    @Autowired
    private GmallPmsFeign pmsFeign;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "GMALL-PMS-QUEUE",durable = "true"),
            exchange = @Exchange(value = "GMALL-PMS-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.insert"}

    ))
    public void listener(Long spuId){
        //没有查询spuEntity的实体类，所以需要将其导入
        Resp<SpuInfoEntity> spuInfoEntityResp = pmsFeign.querySpuEntityById(spuId);
        SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
        Long id = spuInfoEntity.getId();
        Resp<List<SkuInfoEntity>> listResp1 = pmsFeign.querySkuBySpuId(id);
        List<SkuInfoEntity> skuInfoEntities = listResp1.getData();
        if(!CollectionUtils.isEmpty(skuInfoEntities)){
            //elasticsearchRepstriy有一个批量的存储
            List<Goods> goodsList = skuInfoEntities.stream().map(skuInfoEntity -> {
                Goods goods = new Goods();
                goods.setSkuId(skuInfoEntity.getSkuId());
                goods.setPic(skuInfoEntity.getSkuDefaultImg());
                goods.setPrice(skuInfoEntity.getPrice().doubleValue());
                goods.setSale(10l);
                goods.setCreateTime(spuInfoEntity.getCreateTime());
                goods.setCategoryId(skuInfoEntity.getCatalogId());
                Resp<CategoryEntity> categoryEntityResp = pmsFeign.queryCategoryById(skuInfoEntity.getCatalogId());
                goods.setCategoryName(categoryEntityResp.getData().getName());
                goods.setBrandId(skuInfoEntity.getBrandId());
                Resp<BrandEntity> brandEntityResp = pmsFeign.queryBrandById(skuInfoEntity.getBrandId());
                goods.setBrandName(brandEntityResp.getData().getName());
                Resp<List<WareSkuEntity>> listResp2 = wmsFeign.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = listResp2.getData();
                if(!CollectionUtils.isEmpty(wareSkuEntities)){
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()>0));
                }
                goods.setSkuTitle(skuInfoEntity.getSkuTitle());
                goods.setSkuSubTitle(skuInfoEntity.getSkuSubtitle());
                Resp<List<ProductAttrValueEntity>> listResp3 = pmsFeign.querySearchAttrBySpuId(skuInfoEntity.getSpuId());
                List<ProductAttrValueEntity> productAttrValueEntities = listResp3.getData();
                if(!CollectionUtils.isEmpty(productAttrValueEntities)){
                    List<SearchAttrValue> searchAttrValueList = productAttrValueEntities.stream().map(productAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        searchAttrValue.setAttrId(productAttrValueEntity.getAttrId());
                        searchAttrValue.setAttrName(productAttrValueEntity.getAttrName());
                        searchAttrValue.setAttrValue(productAttrValueEntity.getAttrValue());
                        return searchAttrValue;
                    }).collect(Collectors.toList());
                    goods.setAttrs(searchAttrValueList);
                }

                return goods;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);
        }

    }
}
