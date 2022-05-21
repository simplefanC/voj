package com.simplefanc.voj.backend.controller.file;

import com.simplefanc.voj.backend.service.file.ProblemFileService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/10/5 20:05
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class ProblemFileController {

    private final ProblemFileService problemFileService;

    /**
     * @param file
     * @MethodName importProblem
     * @Description zip文件导入题目 仅超级管理员可操作
     * @Return
     * @Since 2021/5/27
     */
    @RequiresRoles("root")
    @RequiresAuthentication
    @ResponseBody
    @PostMapping("/import-problem")
    public CommonResult<Void> importProblem(@RequestParam("file") MultipartFile file) {
        problemFileService.importProblem(file);
        return CommonResult.successResponse();
    }

    /**
     * @param pidList
     * @param response
     * @MethodName exportProblem
     * @Description 导出指定的题目包括测试数据生成zip 仅超级管理员可操作
     * @Return
     * @Since 2021/5/28
     */
    @GetMapping("/export-problem")
    @RequiresAuthentication
    @RequiresRoles("root")
    public void exportProblem(@RequestParam("pid") List<Long> pidList, HttpServletResponse response) {
        problemFileService.exportProblem(pidList, response);
    }

}