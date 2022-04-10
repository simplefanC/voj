package com.simplefanc.voj.service.admin.training;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.pojo.dto.TrainingDto;
import com.simplefanc.voj.pojo.entity.training.Training;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 19:46
 * @Description:
 */
public interface AdminTrainingService {
    IPage<Training> getTrainingList(Integer limit, Integer currentPage, String keyword);

    TrainingDto getTraining(Long tid);

    void deleteTraining(Long tid);

    void addTraining(TrainingDto trainingDto);

    void updateTraining(TrainingDto trainingDto);

    void changeTrainingStatus(Long tid, String author, Boolean status);


}