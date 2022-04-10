package com.simplefanc.voj.service.admin.problem;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.pojo.dto.ProblemDto;
import com.simplefanc.voj.pojo.dto.CompileDTO;
import com.simplefanc.voj.pojo.entity.problem.Problem;
import com.simplefanc.voj.pojo.entity.problem.ProblemCase;

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

    void addProblem(ProblemDto problemDto);

    void updateProblem(ProblemDto problemDto);

    List<ProblemCase> getProblemCases(Long pid, Boolean isUpload);

    CommonResult compileSpj(CompileDTO compileDTO);

    CommonResult compileInteractive(CompileDTO compileDTO);

    void importRemoteOJProblem(String name, String problemId);

    void changeProblemAuth(Problem problem);


}