package com.simplefanc.voj.backend.controller.admin;

import com.simplefanc.voj.backend.pojo.dto.SwitchConfigDTO;
import com.simplefanc.voj.backend.service.admin.system.ConfigService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Author chenfan
 * @Date 2022/9/20
 */
@RestController
@RequestMapping("/api/admin/switch")
@RequiredArgsConstructor
public class SwitchController {

    private final ConfigService configService;

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/info")
    public CommonResult<SwitchConfigDTO> getSwitchConfig() {
        return CommonResult.successResponse(configService.getSwitchConfig());
    }

    @RequiresPermissions("system_info_admin")
    @PutMapping("/update")
    public CommonResult<Void> setSwitchConfig(@RequestBody SwitchConfigDTO config) {
        configService.setSwitchConfig(config);
        return CommonResult.successResponse();
    }
}
