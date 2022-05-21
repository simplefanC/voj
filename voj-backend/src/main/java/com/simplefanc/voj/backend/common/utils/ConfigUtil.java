package com.simplefanc.voj.backend.common.utils;

import com.simplefanc.voj.backend.pojo.vo.ConfigVo;
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

    private final ConfigVo configVo;

    public String getConfigContent() {
        return buildYamlStr(configVo);
    }

    public String buildYamlStr(ConfigVo configVo) {
        return "voj:\n" +
                "  jwt:\n" +
                "    # 加密秘钥\n" +
                "    secret: " + configVo.getTokenSecret() + "\n" +
                "    # token有效时长，1天，单位秒\n" +
                "    expire: " + configVo.getTokenExpire() + "\n" +
                "    checkRefreshExpire: " + configVo.getCheckRefreshExpire() + "\n" +
                "    header: token\n" +
                "  judge:\n" +
                "    # 调用判题服务器的token\n" +
                "    token: " + configVo.getJudgeToken() + "\n" +
                "  db:\n" +
                "    host: " + configVo.getMysqlHost() + "\n" +
                "    public-host: " + configVo.getMysqlPublicHost() + "\n" +
                "    port: " + configVo.getMysqlPort() + "\n" +
                "    name: " + configVo.getMysqlDbName() + "\n" +
                "    username: " + configVo.getMysqlUsername() + "\n" +
                "    password: " + configVo.getMysqlPassword() + "\n" +
                "  mail:\n" +
                "    ssl: " + configVo.getEmailSsl() + "\n" +
                "    username: " + configVo.getEmailUsername() + "\n" +
                "    password: " + configVo.getEmailPassword() + "\n" +
                "    host: " + configVo.getEmailHost() + "\n" +
                "    port: " + configVo.getEmailPort() + "\n" +
                "  redis:\n" +
                "    host: " + configVo.getRedisHost() + "\n" +
                "    port: " + configVo.getRedisPort() + "\n" +
                "    password: " + configVo.getRedisPassword() + "\n" +
                "  web-config:\n" +
                "    base-url: \"" + configVo.getBaseUrl() + "\"\n" +
                "    name: \"" + configVo.getName() + "\"\n" +
                "    short-name: \"" + configVo.getShortName() + "\"\n" +
                "    description: \"" + configVo.getDescription() + "\"\n" +
                "    register: " + configVo.getRegister() + "\n" +
                "    problem: " + configVo.getProblem() + "\n" +
                "    training: " + configVo.getTraining() + "\n" +
                "    contest: " + configVo.getContest() + "\n" +
                "    status: " + configVo.getStatus() + "\n" +
                "    rank: " + configVo.getRank() + "\n" +
                "    discussion: " + configVo.getDiscussion() + "\n" +
                "    introduction: " + configVo.getIntroduction() + "\n" +
                "    code-visible-start-time: " + configVo.getCodeVisibleStartTime() + "\n" +
                "    footer:\n" +
                "      record:\n" +
                "        name: \"" + configVo.getRecordName() + "\"\n" +
                "        url: \"" + configVo.getRecordUrl() + "\"\n" +
                "      project:\n" +
                "        name: \"" + configVo.getProjectName() + "\"\n" +
                "        url: \"" + configVo.getProjectUrl() + "\"\n";
    }

}