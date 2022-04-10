package com.simplefanc.voj.controller.admin;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.pojo.entity.user.Session;
import com.simplefanc.voj.service.admin.system.DashboardService;

import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2020/12/6 15:10
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;


    @PostMapping("/get-sessions")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Session> getRecentSession() {
        return CommonResult.successResponse(dashboardService.getRecentSession());
    }

    @GetMapping("/get-dashboard-info")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Map<Object, Object>> getDashboardInfo() {
        return CommonResult.successResponse(dashboardService.getDashboardInfo());
    }
}