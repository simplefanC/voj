package com.simplefanc.voj.backend.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.AdminEditUserDTO;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;
import com.simplefanc.voj.backend.service.admin.user.AdminUserService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2021/12/6 15:18
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/get-user-list")
    @RequiresAuthentication
    @RequiresPermissions("user_admin")
    public CommonResult<IPage<UserRolesVO>> getUserList(@RequestParam(value = "limit", required = false) Integer limit,
                                                        @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                        @RequestParam(value = "roleId", required = false) Long roleId,
                                                        @RequestParam(value = "status", required = false) Integer status,
                                                        @RequestParam(value = "keyword", required = false) String keyword) {
        return CommonResult.successResponse(adminUserService.getUserList(limit, currentPage, keyword, roleId, status));
    }

    @PutMapping("/edit-user")
    @RequiresPermissions("user_admin")
    @RequiresAuthentication
    public CommonResult<Void> editUser(@RequestBody AdminEditUserDTO adminEditUserDTO) {
        adminUserService.editUser(adminEditUserDTO);
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

    @PostMapping("/forbid-user")
    @RequiresPermissions("user_admin")
    @RequiresAuthentication
    public CommonResult<Void> forbidUser(@RequestBody Map<String, Object> params) {
        // TODO 参数
        adminUserService.forbidUser((List<String>) params.get("ids"));
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
        return CommonResult.successResponse(adminUserService.generateUser(params));
    }

}