package com.simplefanc.voj.judger.judge.local.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author: chenfan
 * @Date: 2022/1/2 20:58
 * @Description: 评测题目的传输类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class JudgeDTO implements Serializable {

    private static final long serialVersionUID = 666L;

    /**
     * 当前题目评测点的的编号
     */
    private Integer testCaseNum;

    /**
     * 当前题目评测点的输入文件的名字
     */
    private String testCaseInputFileName;

    /**
     * 当前题目评测点的输入文件的绝对路径
     */
    private String testCaseInputPath;

    /**
     * 当前题目评测点的输出文件的名字
     */
    private String testCaseOutputFileName;

    /**
     * 当前题目评测点的输出文件的绝对路径
     */
    private String testCaseOutputPath;

    /**
     * 数据库表的测试样例id
     */
    private Long problemCaseId;

    /**
     * 当前题目评测点的分数（OI题目的测试点才有）
     */
    private Integer score;

    /**
     * 当前题目评测点的输出字符大小限制 kb
     */
    private Long maxOutputSize;

}