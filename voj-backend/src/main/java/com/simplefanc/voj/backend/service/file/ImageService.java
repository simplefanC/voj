package com.simplefanc.voj.backend.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:31
 * @Description:
 */
public interface ImageService {

    Map<Object, Object> uploadAvatar(MultipartFile image);

    Map<Object, Object> uploadCarouselImg(MultipartFile image);
}