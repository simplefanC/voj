package com.simplefanc.voj.backend.service.admin.rejudge;

import com.simplefanc.voj.common.pojo.entity.judge.Judge;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 16:21
 * @Description:
 */

public interface RejudgeService {

    Judge rejudge(Long submitId);

    void rejudgeContestProblem(Long cid, Long pid);

}