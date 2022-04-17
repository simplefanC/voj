package com.simplefanc.voj.pojo.vo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @Author: chenfan
 * @Date: 2020/12/2 21:30
 * @Description:
 */
@RefreshScope
@Data
@Component
public class ConfigVo {
    // 数据库配置
    @Value("${voj.db.username}")
    private String mysqlUsername;

    @Value("${voj.db.password}")
    private String mysqlPassword;

    @Value("${voj.db.name}")
    private String mysqlDBName;

    @Value("${voj.db.host}")
    private String mysqlHost;

    @Value("${voj.db.public-host}")
    private String mysqlPublicHost;

    @Value("${voj.db.port}")
    private Integer mysqlPort;

    // 判题服务token
    @Value("${voj.judge.token}")
    private String judgeToken;

    // 缓存配置
    @Value("${voj.redis.host}")
    private String redisHost;

    @Value("${voj.redis.port}")
    private Integer redisPort;

    @Value("${voj.redis.password}")
    private String redisPassword;

    // jwt配置
    @Value("${voj.jwt.secret}")
    private String tokenSecret;

    @Value("${voj.jwt.expire}")
    private String tokenExpire;

    @Value("${voj.jwt.checkRefreshExpire}")
    private String checkRefreshExpire;

    // 邮箱配置
    @Value("${voj.mail.username}")
    private String emailUsername;

    @Value("${voj.mail.password}")
    private String emailPassword;

    @Value("${voj.mail.host}")
    private String emailHost;

    @Value("${voj.mail.port}")
    private Integer emailPort;

    @Value("${voj.mail.ssl}")
    private Boolean emailSsl;

    // 网站前端显示配置
    @Value("${voj.web-config.base-url}")
    private String baseUrl;

    @Value("${voj.web-config.name}")
    private String name;

    @Value("${voj.web-config.short-name}")
    private String shortName;

    @Value("${voj.web-config.description}")
    private String description;

    @Value("${voj.web-config.register}")
    private Boolean register;

    @Value("${voj.web-config.problem}")
    private Boolean problem;

    @Value("${voj.web-config.training}")
    private Boolean training;

    @Value("${voj.web-config.contest}")
    private Boolean contest;

    @Value("${voj.web-config.status}")
    private Boolean status;

    @Value("${voj.web-config.rank}")
    private Boolean rank;

    @Value("${voj.web-config.discussion}")
    private Boolean discussion;

    @Value("${voj.web-config.introduction}")
    private Boolean introduction;

    @Value("${voj.web-config.code-visible-start-time}")
    private Long codeVisibleStartTime;

    @Value("${voj.web-config.footer.record.name}")
    private String recordName;

    @Value("${voj.web-config.footer.record.url}")
    private String recordUrl;

    @Value("${voj.web-config.footer.project.name}")
    private String projectName;

    @Value("${voj.web-config.footer.project.url}")
    private String projectUrl;

}
