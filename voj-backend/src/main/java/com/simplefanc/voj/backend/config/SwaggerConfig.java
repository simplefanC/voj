package com.simplefanc.voj.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;

/**
 * @Author: chenfan
 * @Date: 2020/5/29 22:28
 * @Description:
 */
//@Configuration
//@EnableSwagger2 //开启swagger2
public class SwaggerConfig {
    @Bean //配置swagger的docket的bean势力
    public Docket docket(Environment environment) {
        // 设置要显示的swagger环境
        // 线下环境
        Profiles profiles = Profiles.of("dev", "test");
        // 通过环境判断是否在自己所设定的环境当中
        boolean flag = environment.acceptsProfiles(profiles);
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                //分组
                .groupName("chenfan")
                //开启
                .enable(flag)
                .select()
                //RequestHandlerSelectors扫描方式
                //any()全部
                //none 都不扫描
                //path 过滤什么路径
                .apis(RequestHandlerSelectors.basePackage("com.simplefanc.voj"))
                .build();
    }

    /**
     * 配置swagger信息
     *
     * @return
     */
    private ApiInfo apiInfo() {
        //作者信息
        Contact contact = new Contact("chenfan", "", "");
        return new ApiInfo(
                "VOJ",
                "VOJ后台相关接口文档",
                "1.0",
                "",
                contact,
                "Apache 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList());
    }
}