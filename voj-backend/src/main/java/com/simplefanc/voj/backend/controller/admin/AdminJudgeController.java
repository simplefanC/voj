package com.simplefanc.voj.backend.controller.admin;

import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.backend.service.admin.rejudge.RejudgeService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: chenfan
 * @Date: 2021/1/3 14:09
 * @Description: 超管重判提交
 */

@RestController
@RequestMapping("/api/admin/judge")
public class AdminJudgeController {

    @Resource
    private RejudgeService rejudgeService;

    @GetMapping("/rejudge")
    @RequiresAuthentication
    @RequiresRoles("root")  // 只有超级管理员能操作
    @RequiresPermissions("rejudge")
    public CommonResult<Judge> rejudge(@RequestParam("submitId") Long submitId) {
        Judge judge = rejudgeService.rejudge(submitId);
        return CommonResult.successResponse(judge, "重判成功！该提交已进入判题队列！");
    }

    @GetMapping("/rejudge-contest-problem")
    @RequiresAuthentication
    @RequiresRoles("root")  // 只有超级管理员能操作
    @RequiresPermissions("rejudge")
    public CommonResult<Void> rejudgeContestProblem(@RequestParam("cid") Long cid, @RequestParam("pid") Long pid) {
        rejudgeService.rejudgeContestProblem(cid, pid);
        return CommonResult.successResponse("重判成功！该题目对应的全部提交已进入判题队列！");
    }
}
