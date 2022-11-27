package com.simplefanc.voj.backend.service.admin.system;

import cn.hutool.json.JSONObject;
import com.simplefanc.voj.backend.pojo.dto.*;
import com.simplefanc.voj.common.result.CommonResult;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:50
 * @Description: 动态修改网站配置，获取后台服务状态及判题服务器的状态
 */
public interface ConfigService {

    /**
     * @MethodName getServiceInfo
     * @Params * @param null
     * @Description 获取当前服务的相关信息以及当前系统的cpu情况，内存使用情况
     * @Return CommonResult
     * @Since 2021/12/3
     */
    JSONObject getServiceInfo();

    List<JSONObject> getJudgeServiceInfo();

    WebConfigDTO getWebConfig();

    void setWebConfig(WebConfigDTO webConfigDTO);

    void deleteHomeCarousel(Long id);

    EmailConfigDTO getEmailConfig();

    void setEmailConfig(EmailConfigDTO config);

    void testEmail(TestEmailDTO testEmailDTO);

    DbAndRedisConfigDTO getDbAndRedisConfig();

    void setDbAndRedisConfig(DbAndRedisConfigDTO config);

    boolean sendNewConfigToNacos();

    SwitchConfigDTO getSwitchConfig();

    void setSwitchConfig(SwitchConfigDTO config);
}