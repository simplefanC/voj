package com.simplefanc.voj.service.admin.system;

import cn.hutool.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @Since 2020/12/3
     */
    JSONObject getServiceInfo();

    List<JSONObject> getJudgeServiceInfo();

    Map<Object, Object> getWebConfig();

    void setWebConfig(HashMap<String, Object> params);

    void deleteHomeCarousel(Long id);

    Map<Object, Object> getEmailConfig();

    void setEmailConfig(HashMap<String, Object> params);


    void testEmail(HashMap<String, Object> params);

    Map<Object, Object> getDBAndRedisConfig();

    void setDBAndRedisConfig(HashMap<String, Object> params);

    boolean sendNewConfigToNacos();
}