package com.simplefanc.voj.judger.service;

import com.simplefanc.voj.common.pojo.dto.ToJudge;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.judger.common.exception.SystemError;

import java.util.HashMap;

public interface JudgeService {

    void judge(Judge judge);

    void remoteJudge(ToJudge toJudge);

    Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles) throws SystemError;

    Boolean compileInteractive(String code, Long pid, String interactiveLanguage, HashMap<String, String> extraFiles) throws SystemError;

    void updateOtherTable(Judge judge);
}
