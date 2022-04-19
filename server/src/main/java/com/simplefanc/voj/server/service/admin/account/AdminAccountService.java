package com.simplefanc.voj.server.service.admin.account;


import com.simplefanc.voj.server.pojo.dto.LoginDto;

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