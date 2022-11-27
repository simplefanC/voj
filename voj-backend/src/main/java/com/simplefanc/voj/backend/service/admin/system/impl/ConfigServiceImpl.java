package com.simplefanc.voj.backend.service.admin.system.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.oshi.OshiUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.utils.ConfigUtil;
import com.simplefanc.voj.backend.common.utils.RestTemplateUtil;
import com.simplefanc.voj.backend.config.ConfigVO;
import com.simplefanc.voj.backend.dao.common.FileEntityService;
import com.simplefanc.voj.backend.pojo.dto.*;
import com.simplefanc.voj.backend.service.admin.system.ConfigService;
import com.simplefanc.voj.backend.service.email.EmailService;
import com.simplefanc.voj.common.pojo.entity.common.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:50
 * @Description: 动态修改网站配置，获取后台服务状态及判题服务器的状态
 */
@Service
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigVO configVO;

    private final EmailService emailService;

    private final FileEntityService fileEntityService;

    private final ConfigUtil configUtil;

    private final DiscoveryClient discoveryClient;

    private final RestTemplateUtil restTemplateUtil;

    @Value("${service-url.name}")
    private String judgeServiceName;

    @Value("${spring.application.name}")
    private String currentServiceName;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosServerAddr;

    @Value("voj" + "-" + "${spring.profiles.active}" + ".yml")
    private String dataId;

    @Value("${spring.cloud.nacos.config.group}")
    private String group;

    @Value("${spring.cloud.nacos.config.username}")
    private String nacosUsername;

    @Value("${spring.cloud.nacos.config.password}")
    private String nacosPassword;

    /**
     * @MethodName getServiceInfo
     * @Params @param null
     * @Description 获取当前服务的相关信息以及当前系统的cpu情况，内存使用情况
     * @Return CommonResult
     * @Since 2021/12/3
     */
    @Override
    public JSONObject getServiceInfo() {
        JSONObject result = new JSONObject();

        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(currentServiceName);
        String response = restTemplateUtil.get(nacosServerAddr, "/nacos/v1/ns/operator/metrics", String.class);

        JSONObject jsonObject = JSONUtil.parseObj(response);
        // 获取当前数据后台所在机器环境
        // 当前机器的cpu核数
        int cores = OshiUtil.getCpuInfo().getCpuNum();
        double cpuLoad = 100 - OshiUtil.getCpuInfo().getFree();
        // 当前服务所在机器cpu使用率
        String percentCpuLoad = String.format("%.2f", cpuLoad) + "%";
        // 当前服务所在机器总内存
        double totalVirtualMemory = OshiUtil.getMemory().getTotal();
        // 当前服务所在机器空闲内存
        double freePhysicalMemorySize = OshiUtil.getMemory().getAvailable();
        double value = freePhysicalMemorySize / totalVirtualMemory;
        // 当前服务所在机器内存使用率
        String percentMemoryLoad = String.format("%.2f", (1 - value) * 100) + "%";
        result.set("nacos", jsonObject);
        result.set("backupCores", cores);
        result.set("backupService", serviceInstances);
        result.set("backupPercentCpuLoad", percentCpuLoad);
        result.set("backupPercentMemoryLoad", percentMemoryLoad);
        return result;
    }

    @Override
    public List<JSONObject> getJudgeServiceInfo() {
        List<JSONObject> serviceInfoList = new LinkedList<>();
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(judgeServiceName);
        for (ServiceInstance serviceInstance : serviceInstances) {
            String result = restTemplateUtil.get(serviceInstance.getUri(), "/get-sys-config", String.class);
            JSONObject jsonObject = JSONUtil.parseObj(result);
            jsonObject.set("service", serviceInstance);
            serviceInfoList.add(jsonObject);
        }
        return serviceInfoList;
    }

    @Override
    public WebConfigDTO getWebConfig() {
        return BeanUtil.copyProperties(configVO, WebConfigDTO.class);
//        return WebConfigDTO.builder().baseUrl(UnicodeUtil.toString(configVO.getBaseUrl()))
//                .name(UnicodeUtil.toString(configVO.getName()))
//                .shortName(UnicodeUtil.toString(configVO.getShortName()))
//                .description(UnicodeUtil.toString(configVO.getDescription()))
//                .register(configVO.getRegister())
//                .problem(configVO.getProblem())
//                .training(configVO.getTraining())
//                .contest(configVO.getContest())
//                .status(configVO.getStatus())
//                .rank(configVO.getRank())
//                .discussion(configVO.getDiscussion())
//                .introduction(configVO.getIntroduction())
//                .recordName(UnicodeUtil.toString(configVO.getRecordName()))
//                .recordUrl(UnicodeUtil.toString(configVO.getRecordUrl()))
//                .projectName(UnicodeUtil.toString(configVO.getProjectName()))
//                .projectUrl(UnicodeUtil.toString(configVO.getProjectUrl())).build();
    }

    @Override
    public void setWebConfig(WebConfigDTO webConfigDTO) {
        if (!StrUtil.isEmpty(webConfigDTO.getBaseUrl())) {
            configVO.setBaseUrl(webConfigDTO.getBaseUrl());
        }
        if (!StrUtil.isEmpty(webConfigDTO.getName())) {
            configVO.setName(webConfigDTO.getName());
        }
        if (!StrUtil.isEmpty(webConfigDTO.getShortName())) {
            configVO.setShortName(webConfigDTO.getShortName());
        }
        if (!StrUtil.isEmpty(webConfigDTO.getDescription())) {
            configVO.setDescription(webConfigDTO.getDescription());
        }
//        if (webConfigDTO.getRegister() != null) {
//            configVO.setRegister(webConfigDTO.getRegister());
//        }
//        if (webConfigDTO.getCodeVisibleStartTime() != null) {
//            configVO.setCodeVisibleStartTime(webConfigDTO.getCodeVisibleStartTime());
//        }
        if (!StrUtil.isEmpty(webConfigDTO.getRecordName())) {
            configVO.setRecordName(webConfigDTO.getRecordName());
        }
        if (!StrUtil.isEmpty(webConfigDTO.getRecordUrl())) {
            configVO.setRecordUrl(webConfigDTO.getRecordUrl());
        }
        if (!StrUtil.isEmpty(webConfigDTO.getProjectName())) {
            configVO.setProjectName(webConfigDTO.getProjectName());
        }
        if (!StrUtil.isEmpty(webConfigDTO.getProjectUrl())) {
            configVO.setProjectUrl(webConfigDTO.getProjectUrl());
        }

        boolean isOk = sendNewConfigToNacos();
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    @Override
    public void deleteHomeCarousel(Long id) {
        File imgFile = fileEntityService.getById(id);
        if (imgFile == null) {
            throw new StatusFailException("文件id错误，图片不存在");
        }
        boolean isOk = fileEntityService.removeById(id);
        if (isOk) {
            FileUtil.del(imgFile.getFilePath());
        } else {
            throw new StatusFailException("删除失败！");
        }
    }

    @Override
    public EmailConfigDTO getEmailConfig() {
        return BeanUtil.copyProperties(configVO, EmailConfigDTO.class);
//        return EmailConfigDTO.builder().emailUsername(configVO.getEmailUsername())
//                .emailPassword(configVO.getEmailPassword()).emailHost(configVO.getEmailHost())
//                .emailPort(configVO.getEmailPort()).emailSsl(configVO.getEmailSsl()).build();
    }

    @Override
    public void setEmailConfig(EmailConfigDTO config) {
        if (!StrUtil.isEmpty(config.getEmailHost())) {
            configVO.setEmailHost(config.getEmailHost());
        }
        if (!StrUtil.isEmpty(config.getEmailPassword())) {
            configVO.setEmailPassword(config.getEmailPassword());
        }

        if (config.getEmailPort() != null) {
            configVO.setEmailPort(config.getEmailPort());
        }

        if (!StrUtil.isEmpty(config.getEmailUsername())) {
            configVO.setEmailUsername(config.getEmailUsername());
        }

        if (config.getEmailSsl() != null) {
            configVO.setEmailSsl(config.getEmailSsl());
        }

        boolean isOk = sendNewConfigToNacos();
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    @Override
    public void testEmail(TestEmailDTO testEmailDTO) {
        String email = testEmailDTO.getEmail();
        if (StrUtil.isEmpty(email)) {
            throw new StatusFailException("测试的邮箱不能为空！");
        }
        boolean isEmail = Validator.isEmail(email);
        if (isEmail) {
            emailService.testEmail(email);
        } else {
            throw new StatusFailException("测试的邮箱格式不正确！");
        }
    }

    @Override
    public DbAndRedisConfigDTO getDbAndRedisConfig() {
        return BeanUtil.copyProperties(configVO, DbAndRedisConfigDTO.class);
//        return DbAndRedisConfigDTO.builder().dbName(configVO.getMysqlDbName()).dbHost(configVO.getMysqlHost())
//                .dbPort(configVO.getMysqlPort()).dbUsername(configVO.getMysqlUsername())
//                .dbPassword(configVO.getMysqlPassword()).redisHost(configVO.getRedisHost())
//                .redisPort(configVO.getRedisPort()).redisPassword(configVO.getRedisPassword()).build();
    }

    @Override
    public void setDbAndRedisConfig(DbAndRedisConfigDTO config) {
        if (!StrUtil.isEmpty(config.getDbName())) {
            configVO.setMysqlDbName(config.getDbName());
        }

        if (!StrUtil.isEmpty(config.getDbHost())) {
            configVO.setMysqlHost(config.getDbHost());
        }
        if (config.getDbPort() != null) {
            configVO.setMysqlPort(config.getDbPort());
        }
        if (!StrUtil.isEmpty(config.getDbUsername())) {
            configVO.setMysqlUsername(config.getDbUsername());
        }
        if (!StrUtil.isEmpty(config.getDbPassword())) {
            configVO.setMysqlPassword(config.getDbPassword());
        }

        if (!StrUtil.isEmpty(config.getRedisHost())) {
            configVO.setRedisHost(config.getRedisHost());
        }

        if (config.getRedisPort() != null) {
            configVO.setRedisPort(config.getRedisPort());
        }
        if (!StrUtil.isEmpty(config.getRedisPassword())) {
            configVO.setRedisPassword(config.getRedisPassword());
        }

        boolean isOk = sendNewConfigToNacos();

        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    @Override
    public boolean sendNewConfigToNacos() {
        Properties properties = new Properties();
        properties.put("serverAddr", nacosServerAddr);

        // if need username and password to login
        properties.put("username", nacosUsername);
        properties.put("password", nacosPassword);

        com.alibaba.nacos.api.config.ConfigService configService;
        boolean isOk = false;
        try {
            configService = NacosFactory.createConfigService(properties);
            isOk = configService.publishConfig(dataId, group,
                    configUtil.getConfigContent(), ConfigType.YAML.getType());
        } catch (NacosException e) {
            log.error("通过 Nacos 修改网站配置异常--------------->", e);
        }
        return isOk;
    }

    @Override
    public SwitchConfigDTO getSwitchConfig() {
        return BeanUtil.copyProperties(configVO, SwitchConfigDTO.class);
//        return SwitchConfigDTO.builder()
//                .openPublicDiscussion(configVO.getOpenPublicDiscussion())
//                .openContestComment(configVO.getOpenContestComment())
//                .openPublicJudge(configVO.getOpenPublicJudge())
//                .openContestJudge(configVO.getOpenContestJudge())
//                .defaultSubmitInterval(configVO.getDefaultSubmitInterval())
//                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setSwitchConfig(SwitchConfigDTO config) {
//        if (config.getOpenPublicDiscussion() != null) {
//            configVO.setOpenPublicDiscussion(config.getOpenPublicDiscussion());
//        }
//        if (config.getOpenContestComment() != null) {
//            configVO.setOpenContestComment(config.getOpenContestComment());
//        }
        if (config.getOpenPublicJudge() != null) {
            configVO.setOpenPublicJudge(config.getOpenPublicJudge());
        }
        if (config.getOpenContestJudge() != null) {
            configVO.setOpenContestJudge(config.getOpenContestJudge());
        }
        if (config.getDefaultSubmitInterval() != null) {
            if (config.getDefaultSubmitInterval() >= 0) {
                configVO.setDefaultSubmitInterval(config.getDefaultSubmitInterval());
            } else {
                configVO.setDefaultSubmitInterval(0);
            }
        }
        if (config.getCodeVisibleStartTime() != null) {
            configVO.setCodeVisibleStartTime(config.getCodeVisibleStartTime());
        }

        if (config.getRegister() != null) {
            configVO.setRegister(config.getRegister());
        }
        if (config.getProblem() != null) {
            configVO.setProblem(config.getProblem());
        }
        if (config.getTraining() != null) {
            configVO.setTraining(config.getTraining());
        }
        if (config.getContest() != null) {
            configVO.setContest(config.getContest());
        }
        if (config.getStatus() != null) {
            configVO.setStatus(config.getStatus());
        }
        if (config.getRank() != null) {
            configVO.setRank(config.getRank());
        }
        if (config.getDiscussion() != null) {
            configVO.setDiscussion(config.getDiscussion());
        }
        if (config.getIntroduction() != null) {
            configVO.setIntroduction(config.getIntroduction());
        }
        boolean isOk = sendNewConfigToNacos();
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

}