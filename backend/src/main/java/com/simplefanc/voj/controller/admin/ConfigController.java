package com.simplefanc.voj.controller.admin;


import cn.hutool.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.service.admin.system.ConfigService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2020/12/2 21:42
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    /**
     * @MethodName getServiceInfo
     * @Params * @param null
     * @Description 获取当前服务的相关信息以及当前系统的cpu情况，内存使用情况
     * @Return CommonResult
     * @Since 2020/12/3
     */
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @RequestMapping("/get-service-info")
    public CommonResult<JSONObject> getServiceInfo() {
        return CommonResult.successResponse(configService.getServiceInfo());
    }

    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @RequestMapping("/get-judge-service-info")
    public CommonResult<List<JSONObject>> getJudgeServiceInfo() {
        return CommonResult.successResponse(configService.getJudgeServiceInfo());
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-web-config")
    public CommonResult<Map<Object, Object>> getWebConfig() {
        return CommonResult.successResponse(configService.getWebConfig());
    }


    @RequiresPermissions("system_info_admin")
    @DeleteMapping("/home-carousel")
    public CommonResult<Void> deleteHomeCarousel(@RequestParam("id") Long id) {
        configService.deleteHomeCarousel(id);
        return CommonResult.successResponse();
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping(value = "/set-web-config", method = RequestMethod.PUT)
    public CommonResult<Void> setWebConfig(@RequestBody HashMap<String, Object> params) {
        configService.setWebConfig(params);
        return CommonResult.successResponse();
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-email-config")
    public CommonResult<Map<Object, Object>> getEmailConfig() {
        return CommonResult.successResponse(configService.getEmailConfig());
    }

    @RequiresPermissions("system_info_admin")
    @PutMapping("/set-email-config")
    public CommonResult<Void> setEmailConfig(@RequestBody HashMap<String, Object> params) {
        configService.setEmailConfig(params);
        return CommonResult.successResponse();
    }

    @RequiresPermissions("system_info_admin")
    @PostMapping("/test-email")
    public CommonResult<Void> testEmail(@RequestBody HashMap<String, Object> params) {
        configService.testEmail(params);
        return CommonResult.successResponse();
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-db-and-redis-config")
    public CommonResult<Map<Object, Object>> getDBAndRedisConfig() {
        return CommonResult.successResponse(configService.getDBAndRedisConfig());
    }

    @RequiresPermissions("system_info_admin")
    @PutMapping("/set-db-and-redis-config")
    public CommonResult<Void> setDBAndRedisConfig(@RequestBody HashMap<String, Object> params) {
        configService.setDBAndRedisConfig(params);
        return CommonResult.successResponse();
    }

}