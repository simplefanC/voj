package com.simplefanc.voj.judger.controller;

import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.judger.dao.JudgeServerEntityService;
import com.simplefanc.voj.judger.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2020/12/3 20:12
 * @Description:
 */
@RestController
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private JudgeServerEntityService judgeServerEntityService;

    @RequestMapping("/get-sys-config")
    public HashMap<String, Object> getSystemConfig() {
        return systemConfigService.getSystemConfig();
    }

    @RequestMapping("/version")
    public CommonResult<HashMap<String, Object>> getVersion() {
        return CommonResult.successResponse(judgeServerEntityService.getJudgeServerInfo(), "运行正常");
    }
}