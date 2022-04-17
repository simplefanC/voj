package com.simplefanc.voj.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.pojo.dto.CompileDTO;
import com.simplefanc.voj.pojo.dto.ProblemDto;
import com.simplefanc.voj.pojo.entity.problem.Problem;
import com.simplefanc.voj.pojo.entity.problem.ProblemCase;
import com.simplefanc.voj.service.admin.problem.AdminProblemService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2020/12/11 21:45
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/problem")
public class AdminProblemController {

    @Autowired
    private AdminProblemService adminProblemService;

    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<IPage<Problem>> getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                       @RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "auth", required = false) Integer auth,
                                                       @RequestParam(value = "oj", required = false) String oj) {
        IPage<Problem> problemList = adminProblemService.getProblemList(limit, currentPage, keyword, auth, oj);
        return CommonResult.successResponse(problemList);
    }

    @GetMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Problem> getProblem(@RequestParam("pid") Long pid) {
        Problem problem = adminProblemService.getProblem(pid);
        return CommonResult.successResponse(problem);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteProblem(@RequestParam("pid") Long pid) {
        adminProblemService.deleteProblem(pid);
        return CommonResult.successResponse();
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addProblem(@RequestBody ProblemDto problemDto) {
        adminProblemService.addProblem(problemDto);
        return CommonResult.successResponse();
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateProblem(@RequestBody ProblemDto problemDto) {
        adminProblemService.updateProblem(problemDto);
        return CommonResult.successResponse();
    }

    @GetMapping("/get-problem-cases")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<List<ProblemCase>> getProblemCases(@RequestParam("pid") Long pid,
                                                           @RequestParam(value = "isUpload", defaultValue = "true") Boolean isUpload) {
        List<ProblemCase> problemCaseList = adminProblemService.getProblemCases(pid, isUpload);
        return CommonResult.successResponse(problemCaseList);
    }

    @PostMapping("/compile-spj")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult compileSpj(@RequestBody CompileDTO compileDTO) {
        if (StringUtils.isEmpty(compileDTO.getCode()) ||
                StringUtils.isEmpty(compileDTO.getLanguage())) {
            return CommonResult.errorResponse("参数不能为空！");
        }
        return adminProblemService.compileSpj(compileDTO);
    }

    @PostMapping("/compile-interactive")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult compileInteractive(@RequestBody CompileDTO compileDTO) {
        if (StringUtils.isEmpty(compileDTO.getCode()) ||
                StringUtils.isEmpty(compileDTO.getLanguage())) {
            return CommonResult.errorResponse("参数不能为空！");
        }
        return adminProblemService.compileInteractive(compileDTO);
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> importRemoteOJProblem(@RequestParam("name") String name,
                                                    @RequestParam("problemId") String problemId) {
        adminProblemService.importRemoteOJProblem(name, problemId);
        return CommonResult.successResponse("导入新题目成功");
    }

    @PutMapping("/change-problem-auth")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin", "admin"}, logical = Logical.OR)
    public CommonResult<Void> changeProblemAuth(@RequestBody Problem problem) {
        adminProblemService.changeProblemAuth(problem);
        return CommonResult.successResponse();
    }

}