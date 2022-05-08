package com.simplefanc.voj.backend.service.admin.account;

import com.simplefanc.voj.backend.pojo.dto.LoginDto;

import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 10:10
 * @Description:
 */
public interface AdminAccountService {

    Map<Object, Object> login(LoginDto loginDto);

    void logout();

}