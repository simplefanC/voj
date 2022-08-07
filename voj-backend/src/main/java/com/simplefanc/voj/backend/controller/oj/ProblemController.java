package com.simplefanc.voj.backend.controller.oj;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.pojo.dto.PidListDto;
import com.simplefanc.voj.backend.pojo.vo.ProblemInfoVo;
import com.simplefanc.voj.backend.pojo.vo.ProblemVo;
import com.simplefanc.voj.backend.pojo.vo.RandomProblemVo;
import com.simplefanc.voj.backend.service.oj.ProblemService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/10/27 13:24
 * @Description: 问题数据控制类，处理题目列表请求，题目内容请求。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    /**
     * @param currentPage
     * @param keyword
     * @param tagId
     * @param difficulty
     * @param oj
     * @MethodName getProblemList
     * @Description 获取题目列表分页
     * @Return CommonResult
     * @Since 2021/10/27
     */
    @GetMapping(value = "/get-problem-list")
    public CommonResult<Page<ProblemVo>> getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                                        @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                        @RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "tagId", required = false) List<Long> tagId,
                                                        @RequestParam(value = "difficulty", required = false) Integer difficulty,
                                                        @RequestParam(value = "oj", required = false) String oj) {
        return CommonResult
                .successResponse(problemService.getProblemList(limit, currentPage, keyword, tagId, difficulty, oj));
    }

    /**
     * @MethodName getRandomProblem
     * @Description 随机选取一道题目
     * @Return CommonResult
     * @Since 2021/10/27
     */
    @GetMapping("/get-random-problem")
    public CommonResult<RandomProblemVo> getRandomProblem() {
        return CommonResult.successResponse(problemService.getRandomProblem());
    }

    /**
     * @param pidListDto
     * @MethodName getUserProblemStatus
     * @Description 获取用户对应该题目列表中各个题目的做题情况
     * @Return CommonResult
     * @Since 2021/12/29
     */
    @RequiresAuthentication
    @PostMapping("/get-user-problem-status")
    public CommonResult<HashMap<Long, Object>> getUserProblemStatus(@Validated @RequestBody PidListDto pidListDto) {
        return CommonResult.successResponse(problemService.getUserProblemStatus(pidListDto));
    }

    /**
     * @param problemId
     * @MethodName getProblemInfo
     * @Description 获取指定题目的详情信息，标签，所支持语言，做题情况（只能查询公开题目 也就是auth为1）
     * @Return CommonResult
     * @Since 2021/10/27
     */
    @RequestMapping(value = "/get-problem", method = RequestMethod.GET)
    public CommonResult<ProblemInfoVo> getProblemInfo(
            @RequestParam(value = "problemId") String problemId) {
        return CommonResult.successResponse(problemService.getProblemInfo(problemId));
    }

}