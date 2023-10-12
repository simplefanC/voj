package com.simplefanc.voj.judger.service;

import com.simplefanc.voj.common.pojo.dto.JudgeDTO;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.judger.common.exception.SystemException;

import java.util.HashMap;

public interface JudgeService {

    void localJudge(Judge judge);

    void remoteJudge(JudgeDTO toJudge);

    Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles)
            throws SystemException;

    Boolean compileInteractive(String code, Long pid, String interactiveLanguage, HashMap<String, String> extraFiles)
            throws SystemException;

}
