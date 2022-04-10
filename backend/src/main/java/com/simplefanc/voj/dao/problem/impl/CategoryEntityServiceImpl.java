package com.simplefanc.voj.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.dao.problem.CategoryEntityService;
import com.simplefanc.voj.mapper.CategoryMapper;
import com.simplefanc.voj.pojo.entity.problem.Category;

/**
 * @Author: chenfan
 * @Date: 2021/5/4 22:30
 * @Description:
 */
@Service
public class CategoryEntityServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryEntityService {
}