package com.simplefanc.voj.backend.common.utils;

import com.simplefanc.voj.backend.config.ConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author: chenfan
 * @Date: 2021/12/2 23:17
 * @Description:
 */
@Component
@RequiredArgsConstructor
public class ConfigUtil {

    private final ConfigVO configVO;

    public String getConfigContent() {
        return buildYamlStr(configVO);
    }

    public String buildYamlStr(ConfigVO configVO) {
        return "voj:\n" +
                "  jwt:\n" +
                "    # 加密秘钥\n" +
                "    secret: " + configVO.getTokenSecret() + "\n" +
                "    # token有效时长，1天，单位秒\n" +
                "    expire: " + configVO.getTokenExpire() + "\n" +
                "    checkRefreshExpire: " + configVO.getCheckRefreshExpire() + "\n" +
                "    header: token\n" +
                "  judge:\n" +
                "    # 调用判题服务器的token\n" +
                "    token: " + configVO.getJudgeToken() + "\n" +
                "  db:\n" +
                "    host: " + configVO.getMysqlHost() + "\n" +
                "    public-host: " + configVO.getMysqlPublicHost() + "\n" +
                "    port: " + configVO.getMysqlPort() + "\n" +
                "    name: " + configVO.getMysqlDbName() + "\n" +
                "    username: " + configVO.getMysqlUsername() + "\n" +
                "    password: " + configVO.getMysqlPassword() + "\n" +
                "  mail:\n" +
                "    ssl: " + configVO.getEmailSsl() + "\n" +
                "    username: " + configVO.getEmailUsername() + "\n" +
                "    password: " + configVO.getEmailPassword() + "\n" +
                "    host: " + configVO.getEmailHost() + "\n" +
                "    port: " + configVO.getEmailPort() + "\n" +
                "  redis:\n" +
                "    host: " + configVO.getRedisHost() + "\n" +
                "    port: " + configVO.getRedisPort() + "\n" +
                "    password: " + configVO.getRedisPassword() + "\n" +
                "  switch:\n" +
                "    problem: " + configVO.getProblem() + "\n" +
                "    training: " + configVO.getTraining() + "\n" +
                "    contest: " + configVO.getContest() + "\n" +
                "    status: " + configVO.getStatus() + "\n" +
                "    rank: " + configVO.getRank() + "\n" +
                "    discussion: " + configVO.getDiscussion() + "\n" +
                "    introduction: " + configVO.getIntroduction() + "\n" +
                "    register: " + configVO.getRegister() + "\n" +
                "    judge:\n" +
                "      public: "  + configVO.getOpenPublicJudge() + "\n" +
                "      contest: "  + configVO.getOpenContestJudge() + "\n" +
                "      submit-interval: "  + configVO.getDefaultSubmitInterval() + "\n" +
                "      code-visible-start-time: "  + configVO.getCodeVisibleStartTime() + "\n" +
                "  web-config:\n" +
                "    base-url: \"" + configVO.getBaseUrl() + "\"\n" +
                "    name: \"" + configVO.getName() + "\"\n" +
                "    short-name: \"" + configVO.getShortName() + "\"\n" +
                "    description: \"" + configVO.getDescription() + "\"\n" +
                "    footer:\n" +
                "      record:\n" +
                "        name: \"" + configVO.getRecordName() + "\"\n" +
                "        url: \"" + configVO.getRecordUrl() + "\"\n" +
                "      project:\n" +
                "        name: \"" + configVO.getProjectName() + "\"\n" +
                "        url: \"" + configVO.getProjectUrl() + "\"\n";
    }

}