package com.simplefanc.voj.backend.dao.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;
import com.simplefanc.voj.common.pojo.entity.user.UserRole;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
public interface UserRoleEntityService extends IService<UserRole> {

    UserRolesVO getUserRoles(String uid, String username);

    IPage<UserRolesVO> getUserList(int limit, int currentPage, String keyword, Long role, Integer status);

    void deleteCache(String uid, boolean isRemoveSession);

    String getAuthChangeContent(int oldType, int newType);

}
