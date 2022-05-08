package com.simplefanc.voj.backend.service.file;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:40
 * @Description:
 */
public interface ProblemFileService {

    /**
     * @param file
     * @MethodName importProblem
     * @Description zip文件导入题目 仅超级管理员可操作
     * @Return
     * @Since 2021/5/27
     */
    void importProblem(MultipartFile file);

    /**
     * @param pidList
     * @param response
     * @MethodName exportProblem
     * @Description 导出指定的题目包括测试数据生成zip 仅超级管理员可操作
     * @Return
     * @Since 2021/5/28
     */
    void exportProblem(List<Long> pidList, HttpServletResponse response);

}