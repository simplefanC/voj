package com.simplefanc.voj.controller.admin;

import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.pojo.dto.LoginDto;
import com.simplefanc.voj.service.admin.account.AdminAccountService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * @Author: chenfan
 * @Date: 2020/12/2 21:23
 * @Description:
 */
@RestController
@RequestMapping("/api/admin")
public class AdminAccountController {

    @Autowired
    private AdminAccountService adminAccountService;

    @PostMapping("/login")
    public CommonResult<Void> login(@Validated @RequestBody LoginDto loginDto) {
        adminAccountService.login(loginDto);
        return CommonResult.successResponse("登录成功！");
    }

    @GetMapping("/logout")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> logout() {
        adminAccountService.logout();
        return CommonResult.successResponse("退出登录成功！");
    }

}