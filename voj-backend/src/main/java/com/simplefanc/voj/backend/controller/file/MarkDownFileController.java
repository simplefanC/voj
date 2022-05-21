package com.simplefanc.voj.backend.controller.file;

import com.simplefanc.voj.backend.service.file.MarkDownFileService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2021/10/5 20:01
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class MarkDownFileController {

    private final MarkDownFileService markDownFileService;

    @RequestMapping(value = "/upload-md-img", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Map<Object, Object>> uploadMDImg(@RequestParam("image") MultipartFile image) {
        return CommonResult.successResponse(markDownFileService.uploadMDImg(image));
    }

    @RequestMapping(value = "/delete-md-img", method = RequestMethod.GET)
    @RequiresAuthentication
    @ResponseBody
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteMDImg(@RequestParam("fileId") Long fileId) {
        markDownFileService.deleteMDImg(fileId);
        return CommonResult.successResponse();
    }

    @RequestMapping(value = "/upload-md-file", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Map<Object, Object>> uploadMd(@RequestParam("file") MultipartFile file) {
        return CommonResult.successResponse(markDownFileService.uploadMd(file));
    }

}