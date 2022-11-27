package com.simplefanc.voj.backend.service.admin.problem;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.ProblemDTO;
import com.simplefanc.voj.common.pojo.dto.CompileDTO;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemCase;
import com.simplefanc.voj.common.result.CommonResult;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 16:32
 * @Description:
 */

public interface AdminProblemService {

    IPage<Problem> getProblemList(Integer limit, Integer currentPage, String keyword, Integer auth, String oj);

    Problem getProblem(Long pid);

    void deleteProblem(Long pid);

    void addProblem(ProblemDTO problemDTO);

    void updateProblem(ProblemDTO problemDTO);

    List<ProblemCase> getProblemCases(Long pid, Boolean isUpload);

    CommonResult compileSpj(CompileDTO compileDTO);

    CommonResult compileInteractive(CompileDTO compileDTO);

    void importRemoteOjProblem(String name, String problemId);

    void changeProblemAuth(Problem problem);

}