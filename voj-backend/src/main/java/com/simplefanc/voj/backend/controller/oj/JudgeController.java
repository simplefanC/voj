package com.simplefanc.voj.backend.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.SubmitIdListDTO;
import com.simplefanc.voj.backend.pojo.dto.ToJudgeDTO;
import com.simplefanc.voj.backend.pojo.vo.JudgeVO;
import com.simplefanc.voj.backend.pojo.vo.SubmissionInfoVO;
import com.simplefanc.voj.backend.service.oj.JudgeService;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.judge.JudgeCase;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/10/27 20:52
 * @Description: 处理代码评判相关业务
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JudgeController {

    private final JudgeService judgeService;

    /**
     * @MethodName submitProblemJudge
     * @Description 核心方法 判题通过openfeign调用判题系统服务
     * @Return CommonResult
     * @Since 2021/10/30
     */
    @RequiresAuthentication
    @RequiresPermissions("submit")
    @RequestMapping(value = "/submit-problem-judge", method = RequestMethod.POST)
    public CommonResult<Judge> submitProblemJudge(@RequestBody ToJudgeDTO judgeDTO) {
        return CommonResult.successResponse(judgeService.submitProblemJudge(judgeDTO));
    }

    /**
     * @MethodName resubmit
     * @Description 调用判题服务器提交失败超过60s后，用户点击按钮重新提交判题进入的方法
     * @Return
     * @Since 2021/2/12
     */
    @RequiresAuthentication
    @GetMapping(value = "/resubmit")
    public CommonResult<Judge> resubmit(@RequestParam("submitId") Long submitId) {
        return CommonResult.successResponse(judgeService.resubmit(submitId));
    }

    /**
     * @MethodName getSubmission
     * @Description 获取单个提交记录的详情
     * @Return CommonResult
     * @Since 2021/1/2
     */
    @GetMapping("/submission")
    public CommonResult<SubmissionInfoVO> getSubmission(
            @RequestParam(value = "submitId") Long submitId) {
        return CommonResult.successResponse(judgeService.getSubmission(submitId));
    }

    /**
     * @MethodName updateSubmission
     * @Description 修改单个提交详情的分享权限
     * @Return CommonResult
     * @Since 2021/1/2
     */
    @PutMapping("/submission")
    @RequiresAuthentication
    public CommonResult<Void> updateSubmission(@RequestBody Judge judge) {
        judgeService.updateSubmission(judge);
        return CommonResult.successResponse();
    }

    /**
     * @param limit
     * @param currentPage
     * @param onlyMine
     * @param searchPid
     * @param searchStatus
     * @param searchUsername
     * @param completeProblemId
     * @MethodName getJudgeList
     * @Description 通用查询判题记录列表
     * @Return CommonResult
     * @Since 2021/10/29
     */
    @RequestMapping(value = "/submissions", method = RequestMethod.GET)
    public CommonResult<IPage<JudgeVO>> getJudgeList(@RequestParam(value = "limit", required = false) Integer limit,
                                                     @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                     @RequestParam(value = "onlyMine", required = false) Boolean onlyMine,
                                                     @RequestParam(value = "problemID", required = false) String searchPid,
                                                     @RequestParam(value = "status", required = false) Integer searchStatus,
                                                     @RequestParam(value = "username", required = false) String searchUsername,
                                                     @RequestParam(value = "completeProblemID", defaultValue = "false") Boolean completeProblemId) {
        return CommonResult.successResponse(judgeService.getJudgeList(limit, currentPage, onlyMine, searchPid,
                searchStatus, searchUsername, completeProblemId));
    }

    /**
     * @MethodName checkJudgeResult
     * @Description 对提交列表状态为Pending和Judging的提交进行更新检查
     * @Return
     * @Since 2021/1/3
     */
    @RequestMapping(value = "/check-submissions-status", method = RequestMethod.POST)
    public CommonResult<HashMap<Long, Object>> checkCommonJudgeResult(@RequestBody SubmitIdListDTO submitIdListDTO) {
        return CommonResult.successResponse(judgeService.checkCommonJudgeResult(submitIdListDTO));
    }

    /**
     * @param submitIdListDTO
     * @MethodName checkContestJudgeResult
     * @Description 需要检查是否为封榜，是否可以查询结果，避免有人恶意查询
     * @Return
     * @Since 2021/6/11
     */
    @RequestMapping(value = "/check-contest-submissions-status", method = RequestMethod.POST)
    @RequiresAuthentication
    public CommonResult<HashMap<Long, Object>> checkContestJudgeResult(@RequestBody SubmitIdListDTO submitIdListDTO) {
        return CommonResult.successResponse(judgeService.checkContestJudgeResult(submitIdListDTO));
    }

    /**
     * @param submitId
     * @MethodName getJudgeCase
     * @Description 获得指定提交id的测试样例结果，暂不支持查看测试数据，只可看测试点结果，时间，空间，或者IO得分
     * @Return
     * @Since 2021/10/29
     */
    @GetMapping("/get-all-case-result")
    public CommonResult<List<JudgeCase>> getALLCaseResult(
            @RequestParam(value = "submitId") Long submitId) {
        return CommonResult.successResponse(judgeService.getAllCaseResult(submitId));
    }

}