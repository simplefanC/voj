package com.simplefanc.voj.service;

import com.simplefanc.voj.common.exception.SystemError;
import com.simplefanc.voj.pojo.dto.ToJudge;
import com.simplefanc.voj.pojo.entity.judge.Judge;

import java.util.HashMap;

public interface JudgeService {

    void judge(Judge judge);

    void remoteJudge(ToJudge toJudge);

    Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles) throws SystemError;

    Boolean compileInteractive(String code, Long pid, String interactiveLanguage, HashMap<String, String> extraFiles) throws SystemError;

}
