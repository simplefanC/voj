package com.simplefanc.voj.backend.dao.training.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.training.TrainingCategoryEntityService;
import com.simplefanc.voj.backend.mapper.TrainingCategoryMapper;
import com.simplefanc.voj.common.pojo.entity.training.TrainingCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/11/20 12:15
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class TrainingCategoryEntityServiceImpl extends ServiceImpl<TrainingCategoryMapper, TrainingCategory>
        implements TrainingCategoryEntityService {

    private final TrainingCategoryMapper trainingCategoryMapper;

    @Override
    public TrainingCategory getTrainingCategoryByTrainingId(Long tid) {
        return trainingCategoryMapper.getTrainingCategoryByTrainingId(tid);
    }

}