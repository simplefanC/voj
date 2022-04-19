package com.simplefanc.voj.server.dao.training.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.training.TrainingCategory;
import com.simplefanc.voj.server.dao.training.TrainingCategoryEntityService;
import com.simplefanc.voj.server.mapper.TrainingCategoryMapper;
import org.springframework.stereotype.Service;

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