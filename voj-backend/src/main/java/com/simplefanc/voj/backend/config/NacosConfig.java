package com.simplefanc.voj.backend.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.simplefanc.voj.common.utils.IpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @Author: chenfan
 * @Date: 2022/10/14
 * @Description:
 */
@Configuration
public class NacosConfig {

    @Value("${voj-backend.ip}")
    private String ip;

    @Value("${voj-backend.port}")
    private Integer port;

    /**
     * 用于改变程序自动获取的本机ip
     */
    @Bean
    @Primary
    public NacosDiscoveryProperties nacosProperties() {
        NacosDiscoveryProperties nacosDiscoveryProperties = new NacosDiscoveryProperties();
        // 此处只改了ip，其他参数可以根据自己的需求改变
        nacosDiscoveryProperties.setIp(IpUtil.getServiceIp());
        if (!"-1".equals(ip)) {
            nacosDiscoveryProperties.setIp(ip);
        }
        nacosDiscoveryProperties.setPort(port);

        return nacosDiscoveryProperties;
    }

}