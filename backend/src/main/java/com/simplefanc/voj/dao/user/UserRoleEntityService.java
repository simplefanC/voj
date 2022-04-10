package com.simplefanc.voj.dao.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.user.UserRole;
import com.simplefanc.voj.pojo.vo.UserRolesVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
public interface UserRoleEntityService extends IService<UserRole> {

    UserRolesVo getUserRoles(String uid, String username);

    IPage<UserRolesVo> getUserList(int limit, int currentPage, String keyword, Boolean onlyAdmin);

    void deleteCache(String uid, boolean isRemoveSession);

    String getAuthChangeContent(int oldType, int newType);
}
