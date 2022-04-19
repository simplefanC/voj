package com.simplefanc.voj.server.controller.admin;

import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.server.service.admin.tag.AdminTagService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: chenfan
 * @Date: 2021/11/2 23:24
 * @Description: 处理tag的增删改
 */

@RestController
@RequestMapping("/api/admin/tag")
public class AdminTagController {


    @Resource
    private AdminTagService adminTagService;

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Tag> addProblem(@RequestBody Tag tag) {
        return CommonResult.successResponse(adminTagService.addProblem(tag));
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateTag(@RequestBody Tag tag) {
        adminTagService.updateTag(tag);
        return CommonResult.successResponse();
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteTag(@RequestParam("tid") Long tid) {
        adminTagService.deleteTag(tid);
        return CommonResult.successResponse();
    }

}