package com.simplefanc.voj.judger.judge.local;

import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.judger.common.constants.JudgeLanguage;
import com.simplefanc.voj.judger.common.exception.SystemException;
import com.simplefanc.voj.judger.judge.local.pojo.JudgeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2022/3/12 15:49
 * @Description: 判题上下文
 */
@Component
@RequiredArgsConstructor
public class JudgeContext {

    private final JudgeProcess judgeProcess;

    public void judge(Judge judge, Problem problem) {
        // c和c++为一倍时间和空间，其它语言为2倍时间和空间
        if (!JudgeLanguage.CPP.getLanguage().equals(judge.getLanguage()) &&
                !JudgeLanguage.C.getLanguage().equals(judge.getLanguage()) &&
                !JudgeLanguage.CPPWithO2.getLanguage().equals(judge.getLanguage()) &&
                !JudgeLanguage.CWithO2.getLanguage().equals(judge.getLanguage())) {
            problem.setTimeLimit(problem.getTimeLimit() * 2);
            problem.setMemoryLimit(problem.getMemoryLimit() * 2);
        }

        JudgeResult judgeResult = judgeProcess.execute(problem, judge);
        wrapJudgeResult(judge, judgeResult, problem);
    }

    private void wrapJudgeResult(Judge judge, JudgeResult judgeResult, Problem problem) {
        // 设置最终结果状态码
        judge.setStatus(judgeResult.getStatus());
        judge.setErrorMessage(judgeResult.getErrMsg());
        // 设置最大时间和最大空间不超过题目限制时间和空间
        judge.setMemory(Math.min(judgeResult.getMemory(), problem.getMemoryLimit() * 1024));
        judge.setTime(Math.min(judgeResult.getTime(), problem.getTimeLimit()));
        judge.setScore(judgeResult.getScore());
        judge.setOiRankScore(judgeResult.getOiRankScore());
    }

    public Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles)
            throws SystemException {
        return Compiler.compileSpj(code, pid, spjLanguage, extraFiles);
    }

    public Boolean compileInteractive(String code, Long pid, String interactiveLanguage,
                                      HashMap<String, String> extraFiles) throws SystemException {
        return Compiler.compileInteractive(code, pid, interactiveLanguage, extraFiles);
    }

}