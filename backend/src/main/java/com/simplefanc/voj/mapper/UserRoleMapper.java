package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.pojo.entity.user.Role;
import com.simplefanc.voj.pojo.entity.user.UserRole;
import com.simplefanc.voj.pojo.vo.UserRolesVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface UserRoleMapper extends BaseMapper<UserRole> {

    UserRolesVo getUserRoles(@Param("uid") String uid, @Param("username") String username);

    List<Role> getRolesByUid(@Param("uid") String uid);

    IPage<UserRolesVo> getUserList(Page<UserRolesVo> page, @Param("limit") int limit,
                                   @Param("currentPage") int currentPage,
                                   @Param("keyword") String keyword);

    IPage<UserRolesVo> getAdminUserList(Page<UserRolesVo> page, @Param("limit") int limit,
                                        @Param("currentPage") int currentPage,
                                        @Param("keyword") String keyword);
}
