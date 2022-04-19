package com.simplefanc.voj.server.service.admin.contest;

import com.simplefanc.voj.common.pojo.entity.contest.ContestProblem;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.server.pojo.dto.ContestProblemDto;
import com.simplefanc.voj.server.pojo.dto.ProblemDto;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 11:20
 * @Description:
 */

public interface AdminContestProblemService {

    HashMap<String, Object> getProblemList(Integer limit, Integer currentPage, String keyword,
                                           Long cid, Integer problemType, String oj);

    Problem getProblem(Long pid);

    void deleteProblem(Long pid, Long cid);

    Map<Object, Object> addProblem(ProblemDto problemDto);

    void updateProblem(ProblemDto problemDto);

    ContestProblem getContestProblem(Long cid, Long pid);

    ContestProblem setContestProblem(ContestProblem contestProblem);

    void addProblemFromPublic(ContestProblemDto contestProblemDto);

    void importContestRemoteOJProblem(String name, String problemId, Long cid, String displayId);

}