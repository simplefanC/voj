package com.simplefanc.voj.service.oj;

import com.simplefanc.voj.pojo.dto.ApplyResetPasswordDto;
import com.simplefanc.voj.pojo.dto.LoginDto;
import com.simplefanc.voj.pojo.dto.RegisterDto;
import com.simplefanc.voj.pojo.dto.ResetPasswordDto;
import com.simplefanc.voj.pojo.vo.RegisterCodeVo;
import com.simplefanc.voj.pojo.vo.UserInfoVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 16:46
 * @Description:
 */

public interface PassportService {

    UserInfoVo login(LoginDto loginDto, HttpServletResponse response, HttpServletRequest request);

    RegisterCodeVo getRegisterCode(String email);

    void register(RegisterDto registerDto);

    void applyResetPassword(ApplyResetPasswordDto applyResetPasswordDto);


    void resetPassword(ResetPasswordDto resetPasswordDto);
}