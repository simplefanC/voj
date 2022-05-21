package com.simplefanc.voj.backend.controller.admin;

import com.simplefanc.voj.backend.service.admin.training.AdminTrainingCategoryService;
import com.simplefanc.voj.common.pojo.entity.training.TrainingCategory;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: chenfan
 * @Date: 2021/11/27 15:11
 * @Description:
 */

@RestController
@RequestMapping("/api/admin/training/category")
@RequiredArgsConstructor
public class AdminTrainingCategoryController {

    private final AdminTrainingCategoryService adminTrainingCategoryService;

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<TrainingCategory> addTrainingCategory(@RequestBody TrainingCategory trainingCategory) {
        return CommonResult.successResponse(adminTrainingCategoryService.addTrainingCategory(trainingCategory));
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateTrainingCategory(@RequestBody TrainingCategory trainingCategory) {
        adminTrainingCategoryService.updateTrainingCategory(trainingCategory);
        return CommonResult.successResponse();
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteTrainingCategory(@RequestParam("cid") Long cid) {
        adminTrainingCategoryService.deleteTrainingCategory(cid);
        return CommonResult.successResponse();
    }

}