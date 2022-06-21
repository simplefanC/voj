package com.simplefanc.voj.backend.common.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.common.result.ResultStatus;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenfan
 * @date 2022/5/20 21:06
 **/
@Slf4j
@UtilityClass
public class DownloadFileUtil {
    public void download(HttpServletResponse response, String filePath, String fileName, String errMsg) {
        try(// 放到缓冲流里面
            BufferedInputStream bins = new BufferedInputStream(new FileReader(filePath).getInputStream());
            // 获取文件输出IO流
            BufferedOutputStream bouts = new BufferedOutputStream(response.getOutputStream())
        ) {
            response.setContentType("application/x-download");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, CharsetUtil.UTF_8));
            int bytesRead = 0;
            byte[] buffer = new byte[1024 * 10];
            // 开始向网络传输文件流
            while ((bytesRead = bins.read(buffer, 0, 1024 * 10)) != -1) {
                bouts.write(buffer, 0, bytesRead);
            }
            bouts.flush();
        } catch (IOException e) {
            log.error(errMsg + "------------>", e);
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(CharsetUtil.UTF_8);
            Map<String, Object> map = new HashMap<>();
            map.put("status", ResultStatus.SYSTEM_ERROR);
            map.put("msg", errMsg);
            map.put("data", null);
            try {
                response.getWriter().println(JSONUtil.toJsonStr(map));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
