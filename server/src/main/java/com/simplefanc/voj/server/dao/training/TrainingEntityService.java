package com.simplefanc.voj.server.dao.training;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.training.Training;
import com.simplefanc.voj.server.pojo.vo.TrainingVo;

public interface TrainingEntityService extends IService<Training> {
    IPage<TrainingVo> getTrainingList(int limit, int currentPage,
                                      Long categoryId, String auth, String keyword);

}
