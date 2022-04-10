package com.simplefanc.voj.dao.training;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.training.TrainingProblem;
import com.simplefanc.voj.pojo.vo.ProblemVo;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/11/20 12:24
 * @Description:
 */
public interface TrainingProblemEntityService extends IService<TrainingProblem> {
    public List<Long> getTrainingProblemIdList(Long tid);

    public List<ProblemVo> getTrainingProblemList(Long tid);

    public Integer getUserTrainingACProblemCount(String uid, List<Long> pidList);

}