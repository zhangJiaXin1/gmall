package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryAttrGroupByCid(QueryCondition queryCondition, Long catId) {
        QueryWrapper<AttrGroupEntity> wrapper=new QueryWrapper();
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(queryCondition),
                wrapper.eq("catelog_id", catId));
        PageVo pageVo=new PageVo(page);
        return pageVo;
    }

    @Override
    public GroupVo queryAttrByGid(Long gid) {
        GroupVo groupVo=new GroupVo();
        AttrGroupEntity groupEntities = this.getById(gid);
        BeanUtils.copyProperties(groupEntities,groupVo);
        QueryWrapper<AttrAttrgroupRelationEntity> wrapper=new QueryWrapper();
        //查询出关系表的属性
        List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(wrapper.eq("attr_group_id",gid));
        groupVo.setRelations(relationEntities);
        if(CollectionUtils.isEmpty(relationEntities)){
            return groupVo;
        }
        List<Long> AttrIdList = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        //用这张表去查询所有的属性（attr）
        List<AttrEntity> attrEntities = attrDao.selectBatchIds(AttrIdList);
        groupVo.setAttrEntities(attrEntities);
        return groupVo;
    }

    @Override
    public List<GroupVo> queryAttrAndGroupByCid(Long catId) {
        //通过CatId查询所有的组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));
         return groupEntities.stream().map(attrGroupEntity -> {
            return this.queryAttrByGid(attrGroupEntity.getAttrGroupId());
        }).collect(Collectors.toList());

    }


}