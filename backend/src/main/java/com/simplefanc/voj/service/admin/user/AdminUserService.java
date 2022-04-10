package com.simplefanc.voj.service.admin.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.pojo.dto.AdminEditUserDto;
import com.simplefanc.voj.pojo.vo.UserRolesVo;

import java.util.List;
import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:05
 * @Description:
 */
public interface AdminUserService {

    IPage<UserRolesVo> getUserList(Integer limit, Integer currentPage, Boolean onlyAdmin, String keyword);

    void editUser(AdminEditUserDto adminEditUserDto);

    void deleteUser(List<String> deleteUserIdList);

    void insertBatchUser(List<List<String>> users);

    Map<Object, Object> generateUser(Map<String, Object> params);
}