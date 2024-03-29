package com.simplefanc.voj.backend.pojo.dto;

import com.simplefanc.voj.common.pojo.entity.training.Training;
import com.simplefanc.voj.common.pojo.entity.training.TrainingCategory;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: chenfan
 * @Date: 2021/11/22 21:49
 * @Description: 后台管理训练的传输类
 */
@Data
@Accessors(chain = true)
public class TrainingDTO {

    private Training training;

    private TrainingCategory trainingCategory;

}