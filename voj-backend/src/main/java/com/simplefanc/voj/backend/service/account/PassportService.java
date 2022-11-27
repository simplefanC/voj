package com.simplefanc.voj.backend.service.account;

import com.simplefanc.voj.backend.pojo.dto.ApplyResetPasswordDTO;
import com.simplefanc.voj.backend.pojo.dto.LoginDTO;
import com.simplefanc.voj.backend.pojo.dto.RegisterDTO;
import com.simplefanc.voj.backend.pojo.dto.ResetPasswordDTO;
import com.simplefanc.voj.backend.pojo.vo.RegisterCodeVO;
import com.simplefanc.voj.backend.pojo.vo.UserInfoVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 16:46
 * @Description:
 */

public interface PassportService {

    UserInfoVO login(LoginDTO loginDTO, HttpServletResponse response, HttpServletRequest request);

    RegisterCodeVO getRegisterCode(String email);

    void register(RegisterDTO registerDTO);

    void applyResetPassword(ApplyResetPasswordDTO applyResetPasswordDTO);

    void resetPassword(ResetPasswordDTO resetPasswordDTO);

}