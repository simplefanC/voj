package com.simplefanc.voj.server.dao.training;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.training.TrainingProblem;
import com.simplefanc.voj.server.pojo.vo.ProblemVo;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/11/20 12:24
 * @Description:
 */
public interface TrainingProblemEntityService extends IService<TrainingProblem> {
    List<Long> getTrainingProblemIdList(Long tid);

    List<ProblemVo> getTrainingProblemList(Long tid);

    Integer getUserTrainingACProblemCount(String uid, List<Long> pidList);

}