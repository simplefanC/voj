package com.simplefanc.voj.backend.controller.admin;

import cn.hutool.json.JSONObject;
import com.simplefanc.voj.backend.pojo.dto.DbAndRedisConfigDTO;
import com.simplefanc.voj.backend.pojo.dto.EmailConfigDTO;
import com.simplefanc.voj.backend.pojo.dto.TestEmailDTO;
import com.simplefanc.voj.backend.pojo.dto.WebConfigDTO;
import com.simplefanc.voj.backend.service.admin.system.ConfigService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/12/2 21:42
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    /**
     * @MethodName getServiceInfo
     * @Params * @param null
     * @Description 获取当前服务的相关信息以及当前系统的cpu情况，内存使用情况
     * @Return CommonResult
     * @Since 2021/12/3
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
    public CommonResult<WebConfigDTO> getWebConfig() {
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
    public CommonResult<Void> setWebConfig(@RequestBody WebConfigDTO config) {
        configService.setWebConfig(config);
        return CommonResult.successResponse();
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-email-config")
    public CommonResult<EmailConfigDTO> getEmailConfig() {
        return CommonResult.successResponse(configService.getEmailConfig());
    }

    @RequiresPermissions("system_info_admin")
    @PutMapping("/set-email-config")
    public CommonResult<Void> setEmailConfig(@RequestBody EmailConfigDTO config) {
        configService.setEmailConfig(config);
        return CommonResult.successResponse();
    }

    @RequiresPermissions("system_info_admin")
    @PostMapping("/test-email")
    public CommonResult<Void> testEmail(@RequestBody TestEmailDTO testEmailDTO) {
        configService.testEmail(testEmailDTO);
        return CommonResult.successResponse();
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-db-and-redis-config")
    public CommonResult<DbAndRedisConfigDTO> getDbAndRedisConfig() {
        return CommonResult.successResponse(configService.getDbAndRedisConfig());
    }

    @RequiresPermissions("system_info_admin")
    @PutMapping("/set-db-and-redis-config")
    public CommonResult<Void> setDbAndRedisConfig(@RequestBody DbAndRedisConfigDTO config) {
        configService.setDbAndRedisConfig(config);
        return CommonResult.successResponse();
    }

}