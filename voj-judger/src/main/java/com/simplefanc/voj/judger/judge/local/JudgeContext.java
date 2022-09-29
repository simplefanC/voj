package com.simplefanc.voj.judger.judge.local;

import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.judger.common.constants.JudgeLanguage;
import com.simplefanc.voj.judger.common.exception.SystemError;
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

    public Judge judge(Problem problem, Judge judge) {
        // c和c++为一倍时间和空间，其它语言为2倍时间和空间
        if (!JudgeLanguage.CPP.getLanguage().equals(judge.getLanguage()) &&
                !JudgeLanguage.C.getLanguage().equals(judge.getLanguage()) &&
                !JudgeLanguage.CPPWithO2.getLanguage().equals(judge.getLanguage()) &&
                !JudgeLanguage.CWithO2.getLanguage().equals(judge.getLanguage())) {
            problem.setTimeLimit(problem.getTimeLimit() * 2);
            problem.setMemoryLimit(problem.getMemoryLimit() * 2);
        }

        HashMap<String, Object> judgeResult = judgeProcess.execute(problem, judge);

        return wrapJudgeResult(problem, judge, judgeResult);
    }

    private Judge wrapJudgeResult(Problem problem, Judge judge, HashMap<String, Object> judgeResult) {
        // 如果是编译失败、提交错误、系统错误、运行错误 就有错误提醒
        if (judgeResult.get("code") == JudgeStatus.STATUS_COMPILE_ERROR.getStatus()
                || judgeResult.get("code") == JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus()
                || judgeResult.get("code") == JudgeStatus.STATUS_SYSTEM_ERROR.getStatus()
                || judgeResult.get("code") == JudgeStatus.STATUS_RUNTIME_ERROR.getStatus()) {
            judge.setErrorMessage((String) judgeResult.getOrDefault("errMsg", ""));
        }
        // 设置最终结果状态码
        judge.setStatus((Integer) judgeResult.get("code"));
        // 设置最大时间和最大空间不超过题目限制时间和空间
        // kb
        Integer memory = (Integer) judgeResult.get("memory");
        judge.setMemory(Math.min(memory, problem.getMemoryLimit() * 1024));
        // ms
        Integer time = (Integer) judgeResult.get("time");
        judge.setTime(Math.min(time, problem.getTimeLimit()));
        // score
        judge.setScore((Integer) judgeResult.getOrDefault("score", null));
        // oi_rank_score
        judge.setOiRankScore((Integer) judgeResult.getOrDefault("oiRankScore", null));
        return judge;
    }

    public Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles)
            throws SystemError {
        return Compiler.compileSpj(code, pid, spjLanguage, extraFiles);
    }

    public Boolean compileInteractive(String code, Long pid, String interactiveLanguage,
                                      HashMap<String, String> extraFiles) throws SystemError {
        return Compiler.compileInteractive(code, pid, interactiveLanguage, extraFiles);
    }

}