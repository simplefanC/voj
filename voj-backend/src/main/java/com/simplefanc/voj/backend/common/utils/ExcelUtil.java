package com.simplefanc.voj.backend.common.utils;

import cn.hutool.core.util.CharsetUtil;
import lombok.experimental.UtilityClass;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author chenfan
 * @date 2022/5/20 20:40
 **/
@UtilityClass
public class ExcelUtil {
    public void wrapExcelResponse(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding(CharsetUtil.UTF_8);
        // 这里URLEncoder.encode可以防止中文乱码
        response.setHeader("Content-disposition", "attachment;filename="
                + URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                + ".xlsx");
        response.setHeader("Content-Type", "application/xlsx");
    }
}
