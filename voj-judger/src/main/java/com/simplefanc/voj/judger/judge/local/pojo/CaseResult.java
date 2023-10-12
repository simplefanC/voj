package com.simplefanc.voj.judger.judge.local.pojo;

import lombok.Data;

@Data
public class CaseResult {
    private Integer status;
    private Long time;
    private Long memory;
    private Double percentage; // 交互程序
    private String errMsg;
    private String output;

    private Long caseId;
    private Integer score;
    private String inputFileName;
    private String outputFileName;
}