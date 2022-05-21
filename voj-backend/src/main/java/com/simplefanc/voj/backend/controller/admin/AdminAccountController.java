package com.simplefanc.voj.backend.controller.admin;

import com.simplefanc.voj.backend.pojo.dto.LoginDto;
import com.simplefanc.voj.backend.service.admin.account.AdminAccountService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: chenfan
 * @Date: 2021/12/2 21:23
 * @Description:
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

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