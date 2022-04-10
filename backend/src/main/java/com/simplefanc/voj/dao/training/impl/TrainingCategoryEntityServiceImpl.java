package com.simplefanc.voj.dao.training.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.dao.training.TrainingCategoryEntityService;
import com.simplefanc.voj.mapper.TrainingCategoryMapper;
import com.simplefanc.voj.pojo.entity.training.TrainingCategory;

import javax.annotation.Resource;

/**
 * @Author: chenfan
 * @Date: 2021/11/20 12:15
 * @Description:
 */
@Service
public class TrainingCategoryEntityServiceImpl extends ServiceImpl<TrainingCategoryMapper, TrainingCategory> implements TrainingCategoryEntityService {

    @Resource
    private TrainingCategoryMapper trainingCategoryMapper;

    @Override
    public TrainingCategory getTrainingCategoryByTrainingId(Long tid) {
        return trainingCategoryMapper.getTrainingCategoryByTrainingId(tid);
    }
}