package com.simplefanc.voj.backend.controller.file;

import com.simplefanc.voj.backend.service.file.ImportLOJProblemService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: chenfan
 * @Date: 2022/11/7 19:45
 * @Description:
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class ImportLOJProblemController {

    private final ImportLOJProblemService importLOJProblemService;

    @RequiresRoles("root")
    @RequiresAuthentication
    @GetMapping("/import-loj-problem")
    public CommonResult<Void> importLOJProblem(Integer problemId) {
        importLOJProblemService.importLOJProblem(problemId);
        return CommonResult.successResponse();
    }

}