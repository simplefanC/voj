package com.simplefanc.voj.backend.service.file;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:47
 * @Description:
 */
public interface ImportQDUOJProblemService {

    /**
     * @param file
     * @MethodName importQDOJProblem
     * @Description zip文件导入题目 仅超级管理员可操作
     * @Return
     * @Since 2021/5/27
     */
    void importQDOJProblem(MultipartFile file);

}