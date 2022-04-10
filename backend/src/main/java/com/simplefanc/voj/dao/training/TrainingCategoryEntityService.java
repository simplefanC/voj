package com.simplefanc.voj.dao.training;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.training.TrainingCategory;

public interface TrainingCategoryEntityService extends IService<TrainingCategory> {
    public TrainingCategory getTrainingCategoryByTrainingId(Long tid);
}
