package com.simplefanc.voj.backend.service.file.impl;

import com.alibaba.excel.EasyExcel;
import com.simplefanc.voj.backend.common.constants.Constant;
import com.simplefanc.voj.backend.common.utils.ExcelUtil;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.pojo.vo.ExcelUserVo;
import com.simplefanc.voj.backend.service.file.UserFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 15:02
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class UserFileServiceImpl implements UserFileService {

    private final RedisUtil redisUtil;

    @Override
    public void generateUserExcel(String key, HttpServletResponse response) throws IOException {
        ExcelUtil.wrapExcelResponse(response, key);
        EasyExcel.write(response.getOutputStream(), ExcelUserVo.class)
                .sheet("用户数据")
                .doWrite(getGenerateUsers(key));
    }

    private List<ExcelUserVo> getGenerateUsers(String key) {
        return (List<ExcelUserVo>) redisUtil.hget(Constant.GENERATE_USER_INFO_LIST, key);
    }

}