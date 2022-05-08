package com.simplefanc.voj.backend.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:44
 * @Description:
 */
public interface ImportFpsProblemService {

    /**
     * @param file
     * @MethodName importFpsProblem
     * @Description zip文件导入题目 仅超级管理员可操作
     * @Return
     * @Since 2021/10/06
     */
    void importFPSProblem(MultipartFile file) throws IOException;

}