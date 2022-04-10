package com.simplefanc.voj.dao.training;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.training.Training;
import com.simplefanc.voj.pojo.vo.TrainingVo;

public interface TrainingEntityService extends IService<Training> {
    public IPage<TrainingVo> getTrainingList(int limit, int currentPage,
                                             Long categoryId, String auth, String keyword);

}
