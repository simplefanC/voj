package com.simplefanc.voj.server.controller.file;


import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.server.service.file.ImageService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2021/10/5 19:46
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
public class ImageController {


    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "/upload-avatar", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    public CommonResult<Map<Object, Object>> uploadAvatar(@RequestParam("image") MultipartFile image) {
        return CommonResult.successResponse(imageService.uploadAvatar(image));
    }

    @RequestMapping(value = "/upload-carouse-img", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    @RequiresRoles("root")
    public CommonResult<Map<Object, Object>> uploadCarouselImg(@RequestParam("file") MultipartFile image) {
        return CommonResult.successResponse(imageService.uploadCarouselImg(image));
    }

}