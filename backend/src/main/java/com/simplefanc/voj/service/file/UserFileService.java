package com.simplefanc.voj.service.file;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 15:02
 * @Description:
 */
public interface UserFileService {

    void generateUserExcel(String key, HttpServletResponse response) throws IOException;
}