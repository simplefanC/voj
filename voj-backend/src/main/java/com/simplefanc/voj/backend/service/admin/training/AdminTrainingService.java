package com.simplefanc.voj.backend.service.admin.training;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.TrainingDTO;
import com.simplefanc.voj.common.pojo.entity.training.Training;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 19:46
 * @Description:
 */
public interface AdminTrainingService {

    IPage<Training> getTrainingList(Integer limit, Integer currentPage, String keyword);

    TrainingDTO getTraining(Long tid);

    void deleteTraining(Long tid);

    void addTraining(TrainingDTO trainingDTO);

    void updateTraining(TrainingDTO trainingDTO);

    void changeTrainingStatus(Long tid, String author, Boolean status);

}