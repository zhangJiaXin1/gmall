package com.atguigu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.CategoryDao;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryDao categoryDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoriesByCidOrLevel(Long pid, Integer level) {
        QueryWrapper<CategoryEntity> wrapper=new QueryWrapper<>();
        //如果是默认的和pid=0就是查找所有的
        if(pid!=null){
            wrapper.eq("parent_cid",pid);
        }
        if(level!=0){
            wrapper.eq("cat_level",level);
        }
        List<CategoryEntity> categoryEntities = categoryDao.selectList(wrapper);
        return categoryEntities;
    }

}