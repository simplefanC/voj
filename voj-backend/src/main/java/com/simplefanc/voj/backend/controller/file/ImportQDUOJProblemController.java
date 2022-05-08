package com.simplefanc.voj.backend.controller.file;

import com.simplefanc.voj.backend.service.file.ImportQDUOJProblemService;
import com.simplefanc.voj.common.result.CommonResult;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: chenfan
 * @Date: 2021/10/5 19:44
 * @Description:
 */

@Controller
@RequestMapping("/api/file")
public class ImportQDUOJProblemController {

    @Autowired
    private ImportQDUOJProblemService importQDUOJProblemService;

    /**
     * @param file
     * @MethodName importQDOJProblem
     * @Description zip文件导入题目 仅超级管理员可操作
     * @Return
     * @Since 2021/5/27
     */
    @RequiresRoles("root")
    @RequiresAuthentication
    @ResponseBody
    @PostMapping("/import-qdoj-problem")
    public CommonResult<Void> importQDOJProblem(@RequestParam("file") MultipartFile file) {
        importQDUOJProblemService.importQDOJProblem(file);
        return CommonResult.successResponse();
    }

}