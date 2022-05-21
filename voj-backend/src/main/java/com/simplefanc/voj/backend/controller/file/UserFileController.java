package com.simplefanc.voj.backend.controller.file;

import com.simplefanc.voj.backend.service.file.UserFileService;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: chenfan
 * @Date: 2021/10/5 19:48
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class UserFileController {

    private final UserFileService userFileService;

    @RequestMapping("/generate-user-excel")
    @RequiresAuthentication
    @RequiresRoles("root")
    public void generateUserExcel(@RequestParam("key") String key, HttpServletResponse response) throws IOException {
        userFileService.generateUserExcel(key, response);
    }

}