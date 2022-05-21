package com.simplefanc.voj.backend.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.TrainingDto;
import com.simplefanc.voj.backend.pojo.dto.TrainingProblemDto;
import com.simplefanc.voj.backend.service.admin.training.AdminTrainingProblemService;
import com.simplefanc.voj.backend.service.admin.training.AdminTrainingService;
import com.simplefanc.voj.common.pojo.entity.training.Training;
import com.simplefanc.voj.common.pojo.entity.training.TrainingProblem;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2021/11/22 20:57
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/training")
@RequiredArgsConstructor
public class AdminTrainingController {

    private final AdminTrainingService adminTrainingService;

    private final AdminTrainingProblemService adminTrainingProblemService;

    @GetMapping("/list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<IPage<Training>> getTrainingList(@RequestParam(value = "limit", required = false) Integer limit,
                                                         @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                         @RequestParam(value = "keyword", required = false) String keyword) {
        return CommonResult.successResponse(adminTrainingService.getTrainingList(limit, currentPage, keyword));
    }

    @GetMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<TrainingDto> getTraining(@RequestParam("tid") Long tid) {
        TrainingDto training = adminTrainingService.getTraining(tid);
        return CommonResult.successResponse(training);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = "root") // 只有超级管理员能删除训练
    public CommonResult<Void> deleteTraining(@RequestParam("tid") Long tid) {
        adminTrainingService.deleteTraining(tid);
        return CommonResult.successResponse();
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addTraining(@RequestBody TrainingDto trainingDto) {
        adminTrainingService.addTraining(trainingDto);
        return CommonResult.successResponse();
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateTraining(@RequestBody TrainingDto trainingDto) {
        adminTrainingService.updateTraining(trainingDto);
        return CommonResult.successResponse();
    }

    @PutMapping("/change-training-status")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> changeTrainingStatus(@RequestParam(value = "tid") Long tid,
                                                   @RequestParam(value = "author") String author,
                                                   @RequestParam(value = "status") Boolean status) {
        adminTrainingService.changeTrainingStatus(tid, author, status);
        return CommonResult.successResponse();
    }

    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<HashMap<String, Object>> getProblemList(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "queryExisted", defaultValue = "false") Boolean queryExisted,
            @RequestParam(value = "tid") Long tid) {
        HashMap<String, Object> problemMap = adminTrainingProblemService.getProblemList(limit, currentPage, keyword,
                queryExisted, tid);
        return CommonResult.successResponse(problemMap);
    }

    @PutMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateProblem(@RequestBody TrainingProblem trainingProblem) {
        adminTrainingProblemService.updateProblem(trainingProblem);
        return CommonResult.successResponse();
    }

    @DeleteMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteProblem(@RequestParam("pid") Long pid,
                                            @RequestParam(value = "tid", required = false) Long tid) {
        adminTrainingProblemService.deleteProblem(pid, tid);
        return CommonResult.successResponse();
    }

    @PostMapping("/add-problem-from-public")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addProblemFromPublic(@RequestBody TrainingProblemDto trainingProblemDto) {
        adminTrainingProblemService.addProblemFromPublic(trainingProblemDto);
        return CommonResult.successResponse();
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> importTrainingRemoteOJProblem(@RequestParam("name") String name,
                                                            @RequestParam("problemId") String problemId, @RequestParam("tid") Long tid) {
        adminTrainingProblemService.importTrainingRemoteOJProblem(name, problemId, tid);
        return CommonResult.successResponse();
    }

}