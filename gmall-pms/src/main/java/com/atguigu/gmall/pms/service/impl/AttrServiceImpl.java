package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryAttrByCid(QueryCondition queryCondition, Long cid, Integer type) {
        QueryWrapper<AttrEntity> wrapper =new QueryWrapper<>();
        if(type!=null){
            wrapper.eq("attr_type",type);
        }
        wrapper.eq("catelog_id",cid);
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(queryCondition), wrapper);
        PageVo pageVo=new PageVo(page);
        return pageVo;
    }

    @Override
    public void saveAttr(AttrVo attrVo) {
        //新增attr表
        this.save(attrVo);
        attrVo.getAttrId();

        //新增relations表
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity=new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
        attrAttrgroupRelationEntity.setAttrId(attrVo.getAttrId());
        relationDao.insert(attrAttrgroupRelationEntity);
    }

}