package com.simplefanc.voj.judger.controller;

import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.judger.dao.JudgeServerEntityService;
import com.simplefanc.voj.judger.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2021/12/3 20:12
 * @Description:
 */
@RestController
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @RequestMapping("/get-sys-config")
    public HashMap<String, Object> getSystemConfig() {
        return systemConfigService.getSystemConfig();
    }

    @RequestMapping("/version")
    public CommonResult<HashMap<String, Object>> getVersion() {
        return CommonResult.successResponse(systemConfigService.getJudgeServerInfo(), "运行正常");
    }
}