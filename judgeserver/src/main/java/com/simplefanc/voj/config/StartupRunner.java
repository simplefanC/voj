package com.simplefanc.voj.config;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.simplefanc.voj.dao.JudgeServerEntityService;
import com.simplefanc.voj.pojo.entity.judge.JudgeServer;
import com.simplefanc.voj.util.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;


/**
 * @Author: chenfan
 * @Date: 2021/2/19 22:11
 * @Description:项目启动加载类，启动完毕将该判题机加入到redis里面
 */
@Component
@Slf4j(topic = "voj")
public class StartupRunner implements CommandLineRunner {

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();
    @Value("${voj-judge-server.max-task-num}")
    private Integer maxTaskNum;
    @Value("${voj-judge-server.remote-judge.max-task-num}")
    private Integer maxRemoteTaskNum;
    @Value("${voj-judge-server.remote-judge.open}")
    private Boolean openRemoteJudge;
    @Value("${voj-judge-server.name}")
    private String name;
    @Value("${voj-judge-server.ip}")
    private String ip;
    @Value("${voj-judge-server.port}")
    private Integer port;
    @Autowired
    private JudgeServerEntityService judgeServerEntityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) {

        log.info("IP of the current judge server:" + ip);
        log.info("Port of the current judge server:" + port);

        if (maxTaskNum == -1) {
            maxTaskNum = cpuNum + 1;
        }
        if ("-1".equals(ip)) {
            ip = IpUtils.getLocalIpv4Address();
        }
        UpdateWrapper<JudgeServer> judgeServerQueryWrapper = new UpdateWrapper<>();
        judgeServerQueryWrapper.eq("ip", ip).eq("port", port);
        judgeServerEntityService.remove(judgeServerQueryWrapper);
        boolean isOk1 = judgeServerEntityService.save(new JudgeServer()
                .setCpuCore(cpuNum)
                .setIp(ip)
                .setPort(port)
                .setUrl(ip + ":" + port)
                .setMaxTaskNumber(maxTaskNum)
                .setIsRemote(false)
                .setName(name));

        boolean isOk2 = true;
        if (openRemoteJudge) {
            if (maxRemoteTaskNum == -1) {
                maxRemoteTaskNum = cpuNum * 2 + 1;
            }
            isOk2 = judgeServerEntityService.save(new JudgeServer()
                    .setCpuCore(cpuNum)
                    .setIp(ip)
                    .setPort(port)
                    .setUrl(ip + ":" + port)
                    .setMaxTaskNumber(maxRemoteTaskNum)
                    .setIsRemote(true)
                    .setName(name));
        }

        if (!isOk1 || !isOk2) {
            log.error("初始化判题机信息到数据库失败，请重新启动试试！");
        } else {
            HashMap<String, Object> judgeServerInfo = judgeServerEntityService.getJudgeServerInfo();
            log.info("VOJ-JudgeServer had successfully started! The judge config and sandbox config Info: {}", judgeServerInfo);
        }

    }

}