package com.simplefanc.voj.backend.config;

import com.simplefanc.voj.backend.pojo.bo.FilePathProps;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 解决跨域问题
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final FilePathProps filePathProps;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 前端直接通过/public/img/图片名称即可拿到
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /api/public/img/** /api/public/file/**
        registry.addResourceHandler(filePathProps.getImgApi() + "**", filePathProps.getFileApi() + "**")
                .addResourceLocations("file:" + filePathProps.getUserAvatarFolder() + File.separator,
                        "file:" + filePathProps.getMarkdownFileFolder() + File.separator,
                        "file:" + filePathProps.getHomeCarouselFolder() + File.separator,
                        "file:" + filePathProps.getProblemFileFolder() + File.separator);
    }

}