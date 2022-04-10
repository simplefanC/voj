package com.simplefanc.voj.controller.oj;


import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.pojo.dto.ContestPrintDto;
import com.simplefanc.voj.pojo.dto.ContestRankDto;
import com.simplefanc.voj.pojo.dto.RegisterContestDto;
import com.simplefanc.voj.pojo.dto.UserReadContestAnnouncementDto;
import com.simplefanc.voj.pojo.entity.common.Announcement;
import com.simplefanc.voj.pojo.vo.*;
import com.simplefanc.voj.service.oj.ContestService;

import java.util.List;


/**
 * @Author: chenfan
 * @Date: 2020/10/27 21:40
 * @Description: 处理比赛模块的相关数据请求
 */
@RestController
@RequestMapping("/api")
public class ContestController {

    @Autowired
    private ContestService contestService;


    /**
     * @MethodName getContestList
     * @Params * @param null
     * @Description 获取比赛列表分页数据
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @GetMapping("/get-contest-list")
    public CommonResult<IPage<ContestVo>> getContestList(@RequestParam(value = "limit", required = false) Integer limit,
                                                         @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                         @RequestParam(value = "status", required = false) Integer status,
                                                         @RequestParam(value = "type", required = false) Integer type,
                                                         @RequestParam(value = "keyword", required = false) String keyword) {
        return CommonResult.successResponse(contestService.getContestList(limit, currentPage, status, type, keyword));
    }

    /**
     * @MethodName getContestInfo
     * @Description 获得指定比赛的详细信息
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-info")
    @RequiresAuthentication
    public CommonResult<ContestVo> getContestInfo(@RequestParam(value = "cid", required = true) Long cid) {
        return CommonResult.successResponse(contestService.getContestInfo(cid));
    }

    /**
     * @MethodName toRegisterContest
     * @Description 注册比赛
     * @Return
     * @Since 2020/10/28
     */
    @PostMapping("/register-contest")
    @RequiresAuthentication
    public CommonResult<Void> toRegisterContest(@RequestBody RegisterContestDto registerContestDto) {
        contestService.toRegisterContest(registerContestDto);
        return CommonResult.successResponse();
    }

    /**
     * @MethodName getContestAccess
     * @Description 获得指定私有比赛的访问权限或保护比赛的提交权限
     * @Return
     * @Since 2020/10/28
     */
    @RequiresAuthentication
    @GetMapping("/get-contest-access")
    public CommonResult<AccessVo> getContestAccess(@RequestParam(value = "cid") Long cid) {
        return CommonResult.successResponse(contestService.getContestAccess(cid));
    }


    /**
     * @MethodName getContestProblem
     * @Description 获得指定比赛的题目列表
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-problem")
    @RequiresAuthentication
    public CommonResult<List<ContestProblemVo>> getContestProblem(@RequestParam(value = "cid", required = true) Long cid) {
        return CommonResult.successResponse(contestService.getContestProblem(cid));
    }

    @GetMapping("/get-contest-problem-details")
    @RequiresAuthentication
    public CommonResult<ProblemInfoVo> getContestProblemDetails(@RequestParam(value = "cid", required = true) Long cid,
                                                                @RequestParam(value = "displayId", required = true) String displayId) {
        return CommonResult.successResponse(contestService.getContestProblemDetails(cid, displayId));
    }


    @GetMapping("/contest-submissions")
    @RequiresAuthentication
    public CommonResult<IPage<JudgeVo>> getContestSubmissionList(@RequestParam(value = "limit", required = false) Integer limit,
                                                                 @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                                 @RequestParam(value = "onlyMine", required = false) Boolean onlyMine,
                                                                 @RequestParam(value = "problemID", required = false) String displayId,
                                                                 @RequestParam(value = "status", required = false) Integer searchStatus,
                                                                 @RequestParam(value = "username", required = false) String searchUsername,
                                                                 @RequestParam(value = "contestID", required = true) Long searchCid,
                                                                 @RequestParam(value = "beforeContestSubmit", required = true) Boolean beforeContestSubmit,
                                                                 @RequestParam(value = "completeProblemID", defaultValue = "false") Boolean completeProblemID) {

        return CommonResult.successResponse(contestService.getContestSubmissionList(limit,
                currentPage,
                onlyMine,
                displayId,
                searchStatus,
                searchUsername,
                searchCid,
                beforeContestSubmit,
                completeProblemID));
    }


    /**
     * @MethodName getContestRank
     * @Description 获得比赛做题记录以用来排名
     * @Return
     * @Since 2020/10/28
     */
    @PostMapping("/get-contest-rank")
    @RequiresAuthentication
    public CommonResult<IPage> getContestRank(@RequestBody ContestRankDto contestRankDto) {
        return CommonResult.successResponse(contestService.getContestRank(contestRankDto));
    }


    /**
     * @MethodName getContestAnnouncement
     * @Description 获得比赛的通知列表
     * @Return CommonResult
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-announcement")
    @RequiresAuthentication
    public CommonResult<IPage<AnnouncementVo>> getContestAnnouncement(@RequestParam(value = "cid", required = true) Long cid,
                                                                      @RequestParam(value = "limit", required = false) Integer limit,
                                                                      @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        return CommonResult.successResponse(contestService.getContestAnnouncement(cid, limit, currentPage));
    }


    /**
     * @param userReadContestAnnouncementDto
     * @MethodName getContestUserNotReadAnnouncement
     * @Description 根据前端传过来的比赛id以及已阅读的公告提示id列表，排除后获取未阅读的公告
     * @Return
     * @Since 2021/7/17
     */
    @PostMapping("/get-contest-not-read-announcement")
    @RequiresAuthentication
    public CommonResult<List<Announcement>> getContestUserNotReadAnnouncement(@RequestBody UserReadContestAnnouncementDto userReadContestAnnouncementDto) {
        return CommonResult.successResponse(contestService.getContestUserNotReadAnnouncement(userReadContestAnnouncementDto));
    }


    /**
     * @param contestPrintDto
     * @MethodName submitPrintText
     * @Description 提交比赛文本打印内容
     * @Return
     * @Since 2021/9/20
     */
    @PostMapping("/submit-print-text")
    @RequiresAuthentication
    public CommonResult<Void> submitPrintText(@RequestBody ContestPrintDto contestPrintDto) {
        contestService.submitPrintText(contestPrintDto);
        return CommonResult.successResponse();
    }


}