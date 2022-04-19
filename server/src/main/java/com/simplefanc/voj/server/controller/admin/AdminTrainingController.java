package com.simplefanc.voj.server.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.common.pojo.entity.training.Training;
import com.simplefanc.voj.common.pojo.entity.training.TrainingProblem;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.server.pojo.dto.TrainingDto;
import com.simplefanc.voj.server.pojo.dto.TrainingProblemDto;
import com.simplefanc.voj.server.service.admin.training.AdminTrainingProblemService;
import com.simplefanc.voj.server.service.admin.training.AdminTrainingService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2021/11/22 20:57
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/training")
public class AdminTrainingController {

    @Resource
    private AdminTrainingService adminTrainingService;

    @Resource
    private AdminTrainingProblemService adminTrainingProblemService;


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
    public CommonResult<Void> changeTrainingStatus(@RequestParam(value = "tid", required = true) Long tid,
                                                   @RequestParam(value = "author", required = true) String author,
                                                   @RequestParam(value = "status", required = true) Boolean status) {
        adminTrainingService.changeTrainingStatus(tid, author, status);
        return CommonResult.successResponse();
    }

    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<HashMap<String, Object>> getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                                                @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                                @RequestParam(value = "queryExisted", defaultValue = "false") Boolean queryExisted,
                                                                @RequestParam(value = "tid", required = true) Long tid) {
        HashMap<String, Object> problemMap = adminTrainingProblemService.getProblemList(limit, currentPage, keyword, queryExisted, tid);
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
                                                            @RequestParam("problemId") String problemId,
                                                            @RequestParam("tid") Long tid) {
        adminTrainingProblemService.importTrainingRemoteOJProblem(name, problemId, tid);
        return CommonResult.successResponse();
    }

}