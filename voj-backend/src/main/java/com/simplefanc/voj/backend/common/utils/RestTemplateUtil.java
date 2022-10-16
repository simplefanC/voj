package com.simplefanc.voj.backend.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author chenfan
 * @date 2022/10/14 11:54
 **/
@Component
@RequiredArgsConstructor
public class RestTemplateUtil {
    private final RestTemplate restTemplate;

    public <T> T get(URI uri, String path, Class<T> clazz) {
        return restTemplate.getForObject(uri + path, clazz);
    }

    public <T> T get(String uri, String path, Class<T> clazz) {
        return restTemplate.getForObject(uri + path, clazz);
    }

    /**
     *
     * @param uri http://ip:port
     * @param path
     * @param request
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T post(String uri, String path, Object request, Class<T> clazz) {
//        String uri = String.format("%s://%s:%s", scheme, instance.getHost(), instance.getPort());
        return restTemplate.postForObject(uri + path, request, clazz);
    }
}
