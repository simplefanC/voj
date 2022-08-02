package com.simplefanc.voj.judger.judge.local.task;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.common.constants.JudgeDir;
import com.simplefanc.voj.judger.common.constants.RunConfig;
import com.simplefanc.voj.judger.common.exception.SystemError;
import com.simplefanc.voj.judger.judge.local.AbstractJudge;
import com.simplefanc.voj.judger.judge.local.SandboxRun;
import com.simplefanc.voj.judger.judge.local.entity.JudgeDTO;
import com.simplefanc.voj.judger.judge.local.entity.JudgeGlobalDTO;
import com.simplefanc.voj.judger.judge.local.entity.SandBoxRes;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Author: chenfan
 * @Date: 2022/1/2 23:24
 * @Description: 交互评测
 */
@Component
public class InteractiveJudge extends AbstractJudge {

    @Override
    public JSONArray judge(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemError {

        RunConfig runConfig = judgeGlobalDTO.getRunConfig();
        RunConfig interactiveRunConfig = judgeGlobalDTO.getInteractiveRunConfig();

        // 交互程序的路径
        String interactiveExeSrc = JudgeDir.INTERACTIVE_WORKPLACE_DIR + File.separator + judgeGlobalDTO.getProblemId()
                + File.separator + interactiveRunConfig.getExeName();

        String testCaseInputFileName = judgeGlobalDTO.getProblemId() + "_input";
        String testCaseOutputFileName = judgeGlobalDTO.getProblemId() + "_output";

        String userOutputFileName = judgeGlobalDTO.getProblemId() + "_user_output";

        return SandboxRun.interactTestCase(parseRunCommand(runConfig, null, null, null),
                runConfig.getEnvs(), runConfig.getExeName(), judgeGlobalDTO.getUserFileId(),
                judgeGlobalDTO.getUserFileSrc(), judgeGlobalDTO.getTestTime(), judgeGlobalDTO.getMaxMemory(),
                judgeGlobalDTO.getMaxStack(), judgeDTO.getTestCaseInputPath(), testCaseInputFileName,
                judgeDTO.getTestCaseOutputPath(), testCaseOutputFileName, userOutputFileName,
                parseRunCommand(interactiveRunConfig, testCaseInputFileName,
                        userOutputFileName, testCaseOutputFileName),
                interactiveRunConfig.getEnvs(), interactiveExeSrc, interactiveRunConfig.getExeName());
    }

    @Override
    public JSONObject processResult(SandBoxRes sandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO)
            throws SystemError {
        return null;
    }

    @Override
    public JSONObject processMultipleResult(SandBoxRes userSandBoxRes, SandBoxRes interactiveSandBoxRes,
                                            JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) {

        JSONObject result = new JSONObject();

        // 记录错误信息
        StringBuilder errMsg = new StringBuilder();

        int userExitCode = userSandBoxRes.getExitCode();
        result.set("status", userSandBoxRes.getStatus());
        // 如果运行超过题目限制时间，直接TLE
        if (userSandBoxRes.getTime() > judgeGlobalDTO.getMaxTime()) {
            result.set("status", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (userSandBoxRes.getMemory() > judgeGlobalDTO.getMaxMemory() * 1024) {
            // 如果运行超过题目限制空间，直接MLE
            result.set("status", JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
        } else if ((userExitCode != 0 && userExitCode != 13)
                || (userExitCode == 13 && interactiveSandBoxRes.getExitCode() == 0)) {
            // Broken Pipe
            result.set("status", JudgeStatus.STATUS_RUNTIME_ERROR.getStatus());
            if (userExitCode < 32) {
                errMsg.append(String.format("The program return exit status code: %s (%s)\n", userExitCode,
                        SandboxRun.SIGNALS.get(userExitCode)));
            } else {
                errMsg.append(String.format("The program return exit status code: %s\n", userExitCode));
            }
        } else {
            // 根据交互程序的退出状态码及输出进行判断
            JSONObject interactiveCheckRes = checkInteractiveRes(interactiveSandBoxRes);
            int code = interactiveCheckRes.getInt("code");
            if (code == SPJ_WA) {
                result.set("status", JudgeStatus.STATUS_WRONG_ANSWER.getStatus());
            } else if (code == SPJ_AC) {
                result.set("status", JudgeStatus.STATUS_ACCEPTED.getStatus());
            } else if (code == SPJ_PE) {
                result.set("status", JudgeStatus.STATUS_PRESENTATION_ERROR.getStatus());
            } else if (code == SPJ_PC) {
                result.set("status", JudgeStatus.STATUS_PARTIAL_ACCEPTED.getStatus());
                result.set("percentage", interactiveCheckRes.getDouble("percentage"));
            } else {
                result.set("status", JudgeStatus.STATUS_SYSTEM_ERROR.getStatus());
            }

            String spjErrMsg = interactiveCheckRes.getStr("errMsg");
            if (!StrUtil.isEmpty(spjErrMsg)) {
                errMsg.append(spjErrMsg).append(" ");
            }
            if (interactiveSandBoxRes.getExitCode() != 0 && !StrUtil.isEmpty(interactiveSandBoxRes.getStderr())) {
                errMsg.append(
                        String.format("Interactive program exited with code: %s", interactiveSandBoxRes.getExitCode()));
            }
        }
        // kb
        result.set("memory", userSandBoxRes.getMemory());
        // ms
        result.set("time", userSandBoxRes.getTime());

        // 记录该测试点的错误信息
        if (!StrUtil.isEmpty(errMsg.toString())) {
            String str = errMsg.toString();
            result.set("errMsg", str.substring(0, Math.min(1024 * 1024, str.length())));
        }

        return result;
    }

    private JSONObject checkInteractiveRes(SandBoxRes interactiveSandBoxRes) {

        JSONObject result = new JSONObject();

        int exitCode = interactiveSandBoxRes.getExitCode();

        // 获取跑题用户输出或错误输出
        if (!StrUtil.isEmpty(interactiveSandBoxRes.getStderr())) {
            result.set("errMsg", interactiveSandBoxRes.getStderr());
        }

        // 如果程序无异常
        if (interactiveSandBoxRes.getStatus().equals(JudgeStatus.STATUS_ACCEPTED.getStatus())) {
            if (exitCode == JudgeStatus.STATUS_ACCEPTED.getStatus()) {
                result.set("code", SPJ_AC);
            } else {
                result.set("code", exitCode);
            }
        } else if (interactiveSandBoxRes.getStatus().equals(JudgeStatus.STATUS_RUNTIME_ERROR.getStatus())) {
            if (exitCode == SPJ_WA || exitCode == SPJ_ERROR || exitCode == SPJ_AC || exitCode == SPJ_PE
                    || exitCode == SPJ_PC) {
                result.set("code", exitCode);
            } else {
                if (!StrUtil.isEmpty(interactiveSandBoxRes.getStderr())) {
                    // 适配testlib.h 根据错误信息前缀判断
                    return parseTestLibErr(interactiveSandBoxRes.getStderr());
                } else {
                    result.set("code", SPJ_ERROR);
                }
            }
        } else {
            result.set("code", SPJ_ERROR);
        }
        return result;
    }

}