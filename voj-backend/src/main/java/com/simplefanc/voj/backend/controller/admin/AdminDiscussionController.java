package com.simplefanc.voj.backend.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.common.pojo.entity.discussion.DiscussionReport;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.backend.service.admin.discussion.AdminDiscussionService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/5/15 20:35
 * @Description:
 */
@RestController
@RequestMapping("/api/admin")
public class AdminDiscussionController {

    @Autowired
    private AdminDiscussionService adminDiscussionService;

    @PutMapping("/discussion")
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult<Void> updateDiscussion(@RequestBody Discussion discussion) {
        adminDiscussionService.updateDiscussion(discussion);
        return CommonResult.successResponse();
    }

    @DeleteMapping("/discussion")
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult<Void> removeDiscussion(@RequestBody List<Integer> didList) {
        adminDiscussionService.removeDiscussion(didList);
        return CommonResult.successResponse();
    }

    @GetMapping("/discussion-report")
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult<IPage<DiscussionReport>> getDiscussionReport(@RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                                                     @RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage) {
        IPage<DiscussionReport> discussionReportIPage = adminDiscussionService.getDiscussionReport(limit, currentPage);
        return CommonResult.successResponse(discussionReportIPage);
    }

    @PutMapping("/discussion-report")
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult<Void> updateDiscussionReport(@RequestBody DiscussionReport discussionReport) {
        adminDiscussionService.updateDiscussionReport(discussionReport);
        return CommonResult.successResponse();
    }

}