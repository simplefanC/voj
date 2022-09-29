package com.simplefanc.voj.backend.controller.admin;

import com.simplefanc.voj.backend.service.admin.tag.AdminTagService;
import com.simplefanc.voj.common.constants.Constant;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import com.simplefanc.voj.common.pojo.entity.problem.TagClassification;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/11/2 23:24
 * @Description: 处理tag的增删改
 */
@RestController
@RequestMapping("/api/admin/tag")
@RequiredArgsConstructor
public class AdminTagController {

    private final AdminTagService adminTagService;

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Tag> addTag(@RequestBody Tag tag) {
        return CommonResult.successResponse(adminTagService.addTag(tag));
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
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

    @GetMapping("/classification")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<List<TagClassification>> getTagClassification(@RequestParam(value = "oj", defaultValue = Constant.LOCAL) String oj) {
        return CommonResult.successResponse(adminTagService.getTagClassification(oj));
    }

    @PostMapping("/classification")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<TagClassification> addTagClassification(@RequestBody TagClassification tagClassification) {
        return CommonResult.successResponse(adminTagService.addTagClassification(tagClassification));
    }

    @PutMapping("/classification")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateTagClassification(@RequestBody TagClassification tagClassification) {
        adminTagService.updateTagClassification(tagClassification);
        return CommonResult.successResponse();
    }

    @DeleteMapping("/classification")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteTagClassification(@RequestParam("tcid") Long tcid) {
        adminTagService.deleteTagClassification(tcid);
        return CommonResult.successResponse();
    }

}