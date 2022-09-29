package com.simplefanc.voj.backend.service.account;

import com.simplefanc.voj.backend.pojo.dto.ApplyResetPasswordDto;
import com.simplefanc.voj.backend.pojo.dto.LoginDto;
import com.simplefanc.voj.backend.pojo.dto.RegisterDto;
import com.simplefanc.voj.backend.pojo.dto.ResetPasswordDto;
import com.simplefanc.voj.backend.pojo.vo.RegisterCodeVo;
import com.simplefanc.voj.backend.pojo.vo.UserInfoVo;

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