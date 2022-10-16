package com.simplefanc.voj.judger.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.oshi.OshiUtil;
import com.simplefanc.voj.judger.common.constants.JudgeServerConstant;
import com.simplefanc.voj.judger.judge.local.SandboxRun;
import com.simplefanc.voj.judger.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2021/12/3 20:15
 * @Description:
 */
@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    @Value("${voj-judge-server.max-task-num}")
    private Integer maxTaskNum;

    @Value("${voj-judge-server.remote-judge.open}")
    private Boolean isOpenRemoteJudge;

    @Value("${voj-judge-server.remote-judge.max-task-num}")
    private Integer remoteJudgeMaxTaskNum;

    @Value("${voj-judge-server.name}")
    private String judgeServerName;

    @Value("${voj-judge-server.version}")
    private String judgeServerVersion;

    @Override
    public HashMap<String, Object> getSystemConfig() {
        HashMap<String, Object> result = new HashMap<>();
        // cpu核数
        int cpuCores = Runtime.getRuntime().availableProcessors();

        double cpuLoad = 100 - OshiUtil.getCpuInfo().getFree();
        // cpu使用率
        String percentCpuLoad = String.format("%.2f", cpuLoad) + "%";
        // 总内存
        double totalVirtualMemory = OshiUtil.getMemory().getTotal();
        // 空闲内存
        double freePhysicalMemorySize = OshiUtil.getMemory().getAvailable();
        double value = freePhysicalMemorySize / totalVirtualMemory;
        // 内存使用率
        String percentMemoryLoad = String.format("%.2f", (1 - value) * 100) + "%";

        result.put("cpuCores", cpuCores);
        result.put("percentCpuLoad", percentCpuLoad);
        result.put("percentMemoryLoad", percentMemoryLoad);
        return result;
    }

    @Override
    public HashMap<String, Object> getJudgeServerInfo() {
        HashMap<String, Object> res = new HashMap<>();
        res.put("version", judgeServerVersion);
        res.put("currentTime", new Date());
        res.put("judgeServerName", judgeServerName);
        res.put("cpu", Runtime.getRuntime().availableProcessors());
        res.put("languages", JudgeServerConstant.LANGUAGE_LIST);

        if (maxTaskNum == -1) {
            res.put("maxTaskNum", Runtime.getRuntime().availableProcessors() + 1);
        } else {
            res.put("maxTaskNum", maxTaskNum);
        }
        if (isOpenRemoteJudge) {
            res.put("isOpenRemoteJudge", true);
            if (remoteJudgeMaxTaskNum == -1) {
                res.put("remoteJudgeMaxTaskNum", Runtime.getRuntime().availableProcessors() * 2 + 1);
            } else {
                res.put("remoteJudgeMaxTaskNum", remoteJudgeMaxTaskNum);
            }
        }

        String versionResp;
        try {
            versionResp = SandboxRun.getRestTemplate().getForObject(SandboxRun.getSandboxBaseUrl() + "/version",
                    String.class);
        } catch (Exception e) {
            res.put("SandBoxMsg", MapUtil.builder().put("error", e.getMessage()).map());
            return res;
        }

        res.put("SandBoxMsg", JSONUtil.parseObj(versionResp));
        return res;
    }

}