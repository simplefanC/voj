package com.simplefanc.voj.judger.service;

import com.simplefanc.voj.common.pojo.dto.ToJudge;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.judger.common.exception.SystemException;

import java.util.HashMap;

public interface JudgeService {

    void judge(Judge judge);

    void remoteJudge(ToJudge toJudge);

    Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles)
            throws SystemException;

    Boolean compileInteractive(String code, Long pid, String interactiveLanguage, HashMap<String, String> extraFiles)
            throws SystemException;

}
