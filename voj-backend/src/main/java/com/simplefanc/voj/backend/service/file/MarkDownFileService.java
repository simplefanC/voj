package com.simplefanc.voj.backend.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:50
 * @Description:
 */


public interface MarkDownFileService {

    Map<Object, Object> uploadMDImg(MultipartFile image);

    void deleteMDImg(Long fileId);

    Map<Object, Object> uploadMd(MultipartFile file);

}