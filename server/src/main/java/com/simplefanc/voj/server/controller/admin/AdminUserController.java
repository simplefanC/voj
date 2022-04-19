package com.simplefanc.voj.server.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.server.pojo.dto.AdminEditUserDto;
import com.simplefanc.voj.server.pojo.vo.UserRolesVo;
import com.simplefanc.voj.server.service.admin.user.AdminUserService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * @Author: chenfan
 * @Date: 2020/12/6 15:18
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;


    @GetMapping("/get-user-list")
    @RequiresAuthentication
    @RequiresPermissions("user_admin")
    public CommonResult<IPage<UserRolesVo>> getUserList(@RequestParam(value = "limit", required = false) Integer limit,
                                                        @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                        @RequestParam(value = "onlyAdmin", defaultValue = "false") Boolean onlyAdmin,
                                                        @RequestParam(value = "keyword", required = false) String keyword) {
        return CommonResult.successResponse(adminUserService.getUserList(limit, currentPage, onlyAdmin, keyword));
    }

    @PutMapping("/edit-user")
    @RequiresPermissions("user_admin")
    @RequiresAuthentication
    public CommonResult<Void> editUser(@RequestBody AdminEditUserDto adminEditUserDto) {
        adminUserService.editUser(adminEditUserDto);
        return CommonResult.successResponse();
    }

    @DeleteMapping("/delete-user")
    @RequiresPermissions("user_admin")
    @RequiresAuthentication
    public CommonResult<Void> deleteUser(@RequestBody Map<String, Object> params) {
        // TODO 参数
        adminUserService.deleteUser((List<String>) params.get("ids"));
        return CommonResult.successResponse();
    }

    @PostMapping("/insert-batch-user")
    @RequiresPermissions("user_admin")
    @RequiresAuthentication
    public CommonResult<Void> insertBatchUser(@RequestBody Map<String, Object> params) {
        // TODO 参数
        adminUserService.insertBatchUser((List<List<String>>) params.get("users"));
        return CommonResult.successResponse();
    }

    @PostMapping("/generate-user")
    @RequiresPermissions("user_admin")
    @RequiresAuthentication
    public CommonResult<Map<Object, Object>> generateUser(@RequestBody Map<String, Object> params) {
        // TODO 参数
        adminUserService.generateUser(params);
        return CommonResult.successResponse();
    }

}