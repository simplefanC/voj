package com.simplefanc.voj.judger.judge.local;

import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.user.UserAcproblem;
import com.simplefanc.voj.judger.common.exception.SystemError;
import com.simplefanc.voj.judger.dao.ContestRecordEntityService;
import com.simplefanc.voj.judger.dao.UserAcproblemEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2022/3/12 15:49
 * @Description:
 */
@Component
public class JudgeContext {

    @Autowired
    private JudgeStrategy judgeStrategy;

    public Judge judge(Problem problem, Judge judge) {
        // TODO 魔数
        // c和c++为一倍时间和空间，其它语言为2倍时间和空间
        if (!judge.getLanguage().equals("C++") && !judge.getLanguage().equals("C") &&
                !judge.getLanguage().equals("C++ With O2") && !judge.getLanguage().equals("C With O2")) {
            problem.setTimeLimit(problem.getTimeLimit() * 2);
            problem.setMemoryLimit(problem.getMemoryLimit() * 2);
        }

        HashMap<String, Object> judgeResult = judgeStrategy.judge(problem, judge);

        // 如果是编译失败、提交错误或者系统错误就有错误提醒
        if (judgeResult.get("code") == JudgeStatus.STATUS_COMPILE_ERROR.getStatus() ||
                judgeResult.get("code") == JudgeStatus.STATUS_SYSTEM_ERROR.getStatus() ||
                judgeResult.get("code") == JudgeStatus.STATUS_RUNTIME_ERROR.getStatus() ||
                judgeResult.get("code") == JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus()) {
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

    public Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles) throws SystemError {
        return Compiler.compileSpj(code, pid, spjLanguage, extraFiles);
    }

    public Boolean compileInteractive(String code, Long pid, String interactiveLanguage, HashMap<String, String> extraFiles) throws SystemError {
        return Compiler.compileInteractive(code, pid, interactiveLanguage, extraFiles);
    }
}