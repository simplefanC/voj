package com.simplefanc.voj.backend.service.file.impl;

import com.alibaba.excel.EasyExcel;
import com.simplefanc.voj.backend.common.constants.Constant;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.pojo.vo.ExcelUserVo;
import com.simplefanc.voj.backend.service.file.UserFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 15:02
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
public class UserFileServiceImpl implements UserFileService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void generateUserExcel(String key, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode(key, StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Content-Type", "application/xlsx");
        EasyExcel.write(response.getOutputStream(), ExcelUserVo.class).sheet("用户数据").doWrite(getGenerateUsers(key));
    }

    private List<ExcelUserVo> getGenerateUsers(String key) {
        return (List<ExcelUserVo>) redisUtil.hget(Constant.GENERATE_USER_INFO_LIST, key);
    }

}