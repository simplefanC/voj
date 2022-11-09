package com.simplefanc.voj.backend.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.AnnouncementDto;
import com.simplefanc.voj.backend.pojo.dto.ContestProblemDto;
import com.simplefanc.voj.backend.pojo.dto.ProblemDto;
import com.simplefanc.voj.backend.pojo.vo.AdminContestVo;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVo;
import com.simplefanc.voj.backend.service.admin.contest.AdminContestAnnouncementService;
import com.simplefanc.voj.backend.service.admin.contest.AdminContestProblemService;
import com.simplefanc.voj.backend.service.admin.contest.AdminContestService;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestProblem;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2021/12/19 22:28
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/contest")
@RequiredArgsConstructor
public class AdminContestController {

    private final AdminContestService adminContestService;

    private final AdminContestProblemService adminContestProblemService;

    private final AdminContestAnnouncementService adminContestAnnouncementService;

    @GetMapping("/get-contest-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<IPage<Contest>> getContestList(@RequestParam(value = "limit", required = false) Integer limit,
                                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                       @RequestParam(value = "keyword", required = false) String keyword) {

        IPage<Contest> contestList = adminContestService.getContestList(limit, currentPage, keyword);
        return CommonResult.successResponse(contestList);
    }

    @GetMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<AdminContestVo> getContest(@RequestParam("cid") Long cid) {
        AdminContestVo adminContestVo = adminContestService.getContest(cid);
        return CommonResult.successResponse(adminContestVo);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = "root")
    public CommonResult<Void> deleteContest(@RequestParam("cid") Long cid) {
        adminContestService.deleteContest(cid);
        return CommonResult.successResponse();
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addContest(@RequestBody AdminContestVo adminContestVo) {
        adminContestService.addContest(adminContestVo);
        return CommonResult.successResponse();
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateContest(@RequestBody AdminContestVo adminContestVo) {
        adminContestService.updateContest(adminContestVo);
        return CommonResult.successResponse();
    }

    @GetMapping("/clone")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> cloneContest(@RequestParam("cid") Long cid) {
        adminContestService.cloneContest(cid);
        return CommonResult.successResponse();
    }

    @PutMapping("/change-contest-visible")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> changeContestVisible(@RequestParam(value = "cid") Long cid,
                                                   @RequestParam(value = "uid") String uid,
                                                   @RequestParam(value = "visible") Boolean visible) {
        adminContestService.changeContestVisible(cid, uid, visible);
        return CommonResult.successResponse();
    }

    /**
     * 以下为比赛的题目的增删改查操作接口
     *
     * @param limit
     * @param currentPage
     * @param keyword
     * @param cid
     * @param problemType: 0:ACM; 1:OI
     * @param oj
     * @return
     */
    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Map<String, Object>> getProblemList(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "cid") Long cid,
            @RequestParam(value = "problemType", required = false) Integer problemType,
            @RequestParam(value = "oj", required = false) String oj) {
        Map<String, Object> problemList = adminContestProblemService.getProblemList(limit, currentPage, keyword,
                cid, problemType, oj);
        return CommonResult.successResponse(problemList);
    }

    @GetMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Problem> getProblem(@RequestParam("pid") Long pid) {
        Problem problem = adminContestProblemService.getProblem(pid);
        return CommonResult.successResponse(problem);
    }

    @DeleteMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteProblem(@RequestParam("pid") Long pid,
                                            @RequestParam(value = "cid", required = false) Long cid) {
        adminContestProblemService.deleteProblem(pid, cid);
        return CommonResult.successResponse();
    }

    @PostMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Map<Object, Object>> addProblem(@RequestBody ProblemDto problemDto) {
        Map<Object, Object> problemMap = adminContestProblemService.addProblem(problemDto);
        return CommonResult.successResponse(problemMap);
    }

    @PutMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateProblem(@RequestBody ProblemDto problemDto) {
        adminContestProblemService.updateProblem(problemDto);
        return CommonResult.successResponse();
    }

    @GetMapping("/contest-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<ContestProblem> getContestProblem(@RequestParam(value = "cid") Long cid,
                                                          @RequestParam(value = "pid") Long pid) {
        ContestProblem contestProblem = adminContestProblemService.getContestProblem(cid, pid);
        return CommonResult.successResponse(contestProblem);
    }

    @PutMapping("/contest-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<ContestProblem> setContestProblem(@RequestBody ContestProblem contestProblem) {
        return CommonResult.successResponse(adminContestProblemService.setContestProblem(contestProblem));
    }

    @PostMapping("/add-problem-from-public")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addProblemFromPublic(@RequestBody ContestProblemDto contestProblemDto) {
        adminContestProblemService.addProblemFromPublic(contestProblemDto);
        return CommonResult.successResponse();
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> importContestRemoteOJProblem(@RequestParam("name") String name,
                                                           @RequestParam("problemId") String problemId, @RequestParam("cid") Long cid,
                                                           @RequestParam("displayId") String displayId) {
        adminContestProblemService.importContestRemoteOJProblem(name, problemId, cid, displayId);
        return CommonResult.successResponse();
    }

    /**
     * 以下处理比赛公告的操作请求
     */
    @GetMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<IPage<AnnouncementVo>> getAnnouncementList(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "cid") Long cid) {
        IPage<AnnouncementVo> announcementList = adminContestAnnouncementService.getAnnouncementList(limit, currentPage,
                cid);
        return CommonResult.successResponse(announcementList);
    }

    @DeleteMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteAnnouncement(@RequestParam("aid") Long aid) {
        adminContestAnnouncementService.deleteAnnouncement(aid);
        return CommonResult.successResponse();
    }

    @PostMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addAnnouncement(@RequestBody AnnouncementDto announcementDto) {
        adminContestAnnouncementService.addAnnouncement(announcementDto);
        return CommonResult.successResponse();
    }

    @PutMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateAnnouncement(@RequestBody AnnouncementDto announcementDto) {
        adminContestAnnouncementService.updateAnnouncement(announcementDto);
        return CommonResult.successResponse();
    }

}