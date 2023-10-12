package com.simplefanc.voj.judger.judge.local.pojo;

import lombok.Data;

@Data
public class JudgeResult {
    private Integer status;
    private Integer time;
    private Integer memory;
    private Integer score;
    private Integer oiRankScore;
    private String errMsg;
}