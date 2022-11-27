package com.simplefanc.voj.backend.service.admin.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.AdminEditUserDTO;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;

import java.util.List;
import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:05
 * @Description:
 */
public interface AdminUserService {

    IPage<UserRolesVO> getUserList(Integer limit, Integer currentPage, String keyword, Long role, Integer status);

    void editUser(AdminEditUserDTO adminEditUserDTO);

    void deleteUser(List<String> deleteUserIdList);

    void forbidUser(List<String> deleteUserIdList);

    void insertBatchUser(List<List<String>> users);

    Map<Object, Object> generateUser(Map<String, Object> params);

}