package com.simplefanc.voj.backend.service.admin.training;

import com.simplefanc.voj.backend.pojo.dto.TrainingProblemDTO;
import com.simplefanc.voj.common.pojo.entity.training.TrainingProblem;

import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 20:20
 * @Description:
 */

public interface AdminTrainingProblemService {

    HashMap<String, Object> getProblemList(Integer limit, Integer currentPage, String keyword, Boolean queryExisted,
                                           Long tid);

    void updateProblem(TrainingProblem trainingProblem);

    void deleteProblem(Long pid, Long tid);

    void addProblemFromPublic(TrainingProblemDTO trainingProblemDTO);

    void importTrainingRemoteOjProblem(String name, String problemId, Long tid);

}