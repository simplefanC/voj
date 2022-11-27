package com.simplefanc.voj.backend.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: chenfan
 * @Date: 2021/12/10 16:33
 * @Description:
 */
@Data
@Accessors(chain = true)
@ColumnWidth(25)
@AllArgsConstructor
public class ExcelIpVO {

    @ExcelProperty(value = "用户名", index = 0)
    private String username;

    @ExcelProperty(value = "IP", index = 1)
    private String ip;
}