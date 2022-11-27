package com.simplefanc.voj.backend.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.AnnouncementDTO;
import com.simplefanc.voj.backend.pojo.dto.ContestProblemDTO;
import com.simplefanc.voj.backend.pojo.dto.ProblemDTO;
import com.simplefanc.voj.backend.pojo.vo.AdminContestVO;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVO;
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
    public CommonResult<AdminContestVO> getContest(@RequestParam("cid") Long cid) {
        AdminContestVO adminContestVO = adminContestService.getContest(cid);
        return CommonResult.successResponse(adminContestVO);
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
    public CommonResult<Void> addContest(@RequestBody AdminContestVO adminContestVO) {
        adminContestService.addContest(adminContestVO);
        return CommonResult.successResponse();
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateContest(@RequestBody AdminContestVO adminContestVO) {
        adminContestService.updateContest(adminContestVO);
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
    public CommonResult<Map<Object, Object>> addProblem(@RequestBody ProblemDTO problemDTO) {
        Map<Object, Object> problemMap = adminContestProblemService.addProblem(problemDTO);
        return CommonResult.successResponse(problemMap);
    }

    @PutMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateProblem(@RequestBody ProblemDTO problemDTO) {
        adminContestProblemService.updateProblem(problemDTO);
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
    public CommonResult<Void> addProblemFromPublic(@RequestBody ContestProblemDTO contestProblemDTO) {
        adminContestProblemService.addProblemFromPublic(contestProblemDTO);
        return CommonResult.successResponse();
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> importContestRemoteOjProblem(@RequestParam("name") String name,
                                                           @RequestParam("problemId") String problemId, @RequestParam("cid") Long cid) {
        adminContestProblemService.importContestRemoteOjProblem(name, problemId, cid);
        return CommonResult.successResponse();
    }

    /**
     * 以下处理比赛公告的操作请求
     */
    @GetMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<IPage<AnnouncementVO>> getAnnouncementList(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "cid") Long cid) {
        IPage<AnnouncementVO> announcementList = adminContestAnnouncementService.getAnnouncementList(limit, currentPage,
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
    public CommonResult<Void> addAnnouncement(@RequestBody AnnouncementDTO announcementDTO) {
        adminContestAnnouncementService.addAnnouncement(announcementDTO);
        return CommonResult.successResponse();
    }

    @PutMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateAnnouncement(@RequestBody AnnouncementDTO announcementDTO) {
        adminContestAnnouncementService.updateAnnouncement(announcementDTO);
        return CommonResult.successResponse();
    }

}