package com.simplefanc.voj.backend.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * https://www.baeldung.com/spring-boot-yaml-list
 */
@Component
@ConfigurationProperties(prefix = "filepath")
@Data
public class FilePathProperties {

    private String userAvatarFolder;

    /**
     * 主页轮播图
     */
    private String homeCarouselFolder;

    private String markdownFileFolder;

    private String problemFileFolder;

    private String contestTextPrintFolder;

    private String imgApi;

    private String fileApi;

    private String testcaseTmpFolder;

    private String testcaseBaseFolder;

    private String fileDownloadTmpFolder;

    private String contestAcSubmissionTmpFolder;

}