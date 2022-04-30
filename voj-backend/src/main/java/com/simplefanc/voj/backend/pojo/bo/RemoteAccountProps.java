package com.simplefanc.voj.backend.pojo.bo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * https://www.baeldung.com/spring-boot-yaml-list
 */
@Component
@ConfigurationProperties(prefix = "voj.remote")
@Data
public class RemoteAccountProps {

    private List<RemoteOJ> ojs;

    @Data
    public static class RemoteOJ {
        private String oj;
        private List<Account> accounts;
    }

    @Data
    public static class Account {
        private String username;
        private String password;
    }
}