package com.simplefanc.voj.judger.dao.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.judge.JudgeServer;
import com.simplefanc.voj.judger.common.constants.JudgeServerConstant;
import com.simplefanc.voj.judger.dao.JudgeServerEntityService;
import com.simplefanc.voj.judger.judge.local.SandboxRun;
import com.simplefanc.voj.judger.mapper.JudgeServerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2021/4/15 11:27
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
@RefreshScope
public class JudgeServerEntityServiceImpl extends ServiceImpl<JudgeServerMapper, JudgeServer>
        implements JudgeServerEntityService {

    @Value("${voj-judge-server.max-task-num}")
    private Integer maxTaskNum;

    @Value("${voj-judge-server.remote-judge.open}")
    private Boolean isOpenRemoteJudge;

    @Value("${voj-judge-server.remote-judge.max-task-num}")
    private Integer remoteJudgeMaxTaskNum;

    @Value("${voj-judge-server.name}")
    private String name;

    @Override
    public HashMap<String, Object> getJudgeServerInfo() {

        HashMap<String, Object> res = new HashMap<>();
        res.put("version", JudgeServerConstant.VERSION);
        res.put("currentTime", new Date());
        res.put("judgeServerName", name);
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