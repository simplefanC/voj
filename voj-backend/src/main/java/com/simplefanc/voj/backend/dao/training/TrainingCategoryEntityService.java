package com.simplefanc.voj.backend.dao.training;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.training.TrainingCategory;

public interface TrainingCategoryEntityService extends IService<TrainingCategory> {

    TrainingCategory getTrainingCategoryByTrainingId(Long tid);

}
