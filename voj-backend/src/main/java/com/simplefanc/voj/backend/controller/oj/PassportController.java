package com.simplefanc.voj.backend.controller.oj;

import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.backend.pojo.dto.ApplyResetPasswordDto;
import com.simplefanc.voj.backend.pojo.dto.LoginDto;
import com.simplefanc.voj.backend.pojo.dto.RegisterDto;
import com.simplefanc.voj.backend.pojo.dto.ResetPasswordDto;
import com.simplefanc.voj.backend.pojo.vo.RegisterCodeVo;
import com.simplefanc.voj.backend.pojo.vo.UserInfoVo;
import com.simplefanc.voj.backend.service.oj.PassportService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 17:00
 * @Description: 处理登录、注册、重置密码
 */
@RestController
@RequestMapping("/api")
public class PassportController {


    @Autowired
    private PassportService passportService;

    /**
     * @param loginDto
     * @MethodName login
     * @Description 处理登录逻辑
     * @Return CommonResult
     * @Since 2020/10/24
     */
    @PostMapping("/login")
    public CommonResult<UserInfoVo> login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response, HttpServletRequest request) {
        return CommonResult.successResponse(passportService.login(loginDto, response, request));
    }

    /**
     * @MethodName getRegisterCode
     * @Description 调用邮件服务，发送注册流程的6位随机验证码
     * @Return
     * @Since 2020/10/26
     */
    @RequestMapping(value = "/get-register-code", method = RequestMethod.GET)
    public CommonResult<RegisterCodeVo> getRegisterCode(@RequestParam(value = "email", required = true) String email) {
        return CommonResult.successResponse(passportService.getRegisterCode(email));
    }


    /**
     * @param registerDto
     * @MethodName register
     * @Description 注册逻辑，具体参数请看RegisterDto类
     * @Return
     * @Since 2020/10/24
     */
    @PostMapping("/register")
    public CommonResult<Void> register(@Validated @RequestBody RegisterDto registerDto) {
        passportService.register(registerDto);
        return CommonResult.successResponse();
    }


    /**
     * @param applyResetPasswordDto
     * @MethodName applyResetPassword
     * @Description 发送重置密码的链接邮件
     * @Return
     * @Since 2020/11/6
     */
    @PostMapping("/apply-reset-password")
    public CommonResult<Void> applyResetPassword(@RequestBody ApplyResetPasswordDto applyResetPasswordDto) {
        passportService.applyResetPassword(applyResetPasswordDto);
        return CommonResult.successResponse();
    }


    /**
     * @param resetPasswordDto
     * @MethodName resetPassword
     * @Description 用户重置密码
     * @Return
     * @Since 2020/11/6
     */
    @PostMapping("/reset-password")
    public CommonResult<Void> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        passportService.resetPassword(resetPasswordDto);
        return CommonResult.successResponse();
    }


    /**
     * @MethodName logout
     * @Description 退出逻辑，将jwt在redis中清除，下次需要再次登录。
     * @Return CommonResult
     * @Since 2020/10/24
     */
    @GetMapping("/logout")
    @RequiresAuthentication
    public CommonResult<Void> logout() {
        SecurityUtils.getSubject().logout();
        return CommonResult.successResponse("登出成功！");
    }

}