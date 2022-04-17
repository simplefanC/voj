package com.simplefanc.voj.service.impl;


import cn.hutool.system.oshi.OshiUtil;
import com.simplefanc.voj.service.SystemConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2020/12/3 20:15
 * @Description:
 */
@Service
public class SystemConfigServiceImpl implements SystemConfigService {

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

}