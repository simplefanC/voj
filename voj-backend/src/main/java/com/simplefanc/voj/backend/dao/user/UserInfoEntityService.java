package com.simplefanc.voj.backend.dao.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.dto.RegisterDto;
import com.simplefanc.voj.common.pojo.entity.user.UserInfo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
public interface UserInfoEntityService extends IService<UserInfo> {

    Boolean addUser(RegisterDto registerDto);

    List<String> getSuperAdminUidList();

}
