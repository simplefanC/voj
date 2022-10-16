package com.simplefanc.voj.judger.judge.local.strategy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.common.constants.RunConfig;
import com.simplefanc.voj.judger.common.exception.SystemError;
import com.simplefanc.voj.judger.judge.local.AbstractJudge;
import com.simplefanc.voj.judger.judge.local.SandboxRun;
import com.simplefanc.voj.judger.judge.local.pojo.JudgeDTO;
import com.simplefanc.voj.judger.judge.local.pojo.JudgeGlobalDTO;
import com.simplefanc.voj.judger.judge.local.pojo.SandBoxRes;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: chenfan
 * @Date: 2022/1/2 21:18
 * @Description: 普通评测
 */
@Component
public class DefaultJudge extends AbstractJudge {
    private final static Pattern EOL_PATTERN = Pattern.compile("[^\\S\\n]+(?=\\n)");

    @Override
    public JSONArray judgeCase(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemError {
        RunConfig runConfig = judgeGlobalDTO.getRunConfig();
        // 调用安全沙箱使用测试点对程序进行测试
        final List<String> args = parseRunCommand(runConfig, null, null, null);
        return SandboxRun.testCase(args, runConfig.getEnvs(), judgeDTO.getTestCaseInputPath(), judgeGlobalDTO.getTestTime(),
                judgeGlobalDTO.getMaxMemory(), judgeDTO.getMaxOutputSize(), judgeGlobalDTO.getMaxStack(),
                runConfig.getExeName(), judgeGlobalDTO.getUserFileId(), judgeGlobalDTO.getUserFileSrc());
    }

    @Override
    public JSONObject processResult(SandBoxRes sandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) {
        JSONObject result = new JSONObject();

        StringBuilder errMsg = new StringBuilder();
        // 如果测试跑题无异常
        if (sandBoxRes.getStatus().equals(JudgeStatus.STATUS_ACCEPTED.getStatus())) {
            // 比对题目限制、测试数据
            success(sandBoxRes, judgeDTO, judgeGlobalDTO, result);
        } else if (sandBoxRes.getStatus().equals(JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus())) {
            result.set("status", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (sandBoxRes.getExitCode() != 0) {
            // STATUS_RUNTIME_ERROR 记录errMsg
            abort(sandBoxRes, result, errMsg);
        } else {
            result.set("status", sandBoxRes.getStatus());
        }

        result.set("memory", sandBoxRes.getMemory());
        result.set("time", sandBoxRes.getTime());

        // 记录该测试点的错误信息
        if (!StrUtil.isEmpty(errMsg.toString())) {
            String str = errMsg.toString();
            result.set("errMsg", str.substring(0, Math.min(1024 * 1024, str.length())));
        }

        // 如果需要获取用户对于该题目的输出
        if (judgeGlobalDTO.getNeedUserOutputFile()) {
            result.set("output", sandBoxRes.getStdout());
        }

        return result;
    }

    private void abort(SandBoxRes sandBoxRes, JSONObject result, StringBuilder errMsg) {
        result.set("status", JudgeStatus.STATUS_RUNTIME_ERROR.getStatus());
        if (sandBoxRes.getExitCode() < 32) {
            errMsg.append(String.format("The program return exit status code: %s (%s)\n", sandBoxRes.getExitCode(),
                    SandboxRun.SIGNALS.get(sandBoxRes.getExitCode())));
        } else {
            errMsg.append(String.format("The program return exit status code: %s\n", sandBoxRes.getExitCode()));
        }
    }


    private void success(SandBoxRes sandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO, JSONObject result) {
        // 对结果的时间损耗和空间损耗与题目限制做比较，判断是否mle和tle
        if (sandBoxRes.getTime() > judgeGlobalDTO.getMaxTime()) {
            result.set("status", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (sandBoxRes.getMemory() > judgeGlobalDTO.getMaxMemory() * 1024) {
            result.set("status", JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
        } else {
            // 与原测试数据输出的md5进行对比 AC或者是WA
            JSONObject testcaseInfo = (JSONObject) ((JSONArray) judgeGlobalDTO.getTestCaseInfo().get("testCases"))
                    .get(judgeDTO.getTestCaseNum() - 1);
            result.set("status",
                    compareOutput(sandBoxRes.getStdout(), judgeGlobalDTO.getRemoveEOLBlank(), testcaseInfo));
        }
    }

    @Override
    public JSONObject processMultipleResult(SandBoxRes userSandBoxRes, SandBoxRes interactiveSandBoxRes,
                                            JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) {
        return null;
    }

    /**
     * 根据评测结果与用户程序输出的字符串MD5进行对比
     *
     * @param userOutput
     * @param isRemoveEOLBlank
     * @param testcaseInfo
     * @return
     */
    private Integer compareOutput(String userOutput, Boolean isRemoveEOLBlank, JSONObject testcaseInfo) {
        // 如果当前题目选择默认去掉字符串末位空格
        if (isRemoveEOLBlank) {
            String userOutputMd5 = DigestUtils.md5DigestAsHex(rtrim(userOutput).getBytes());
            if (userOutputMd5.equals(testcaseInfo.getStr("EOFStrippedOutputMd5"))) {
                return JudgeStatus.STATUS_ACCEPTED.getStatus();
            }
            return JudgeStatus.STATUS_WRONG_ANSWER.getStatus();
        }
        // 不选择默认去掉文末空格 与原数据进行对比
        String userOutputMd5 = DigestUtils.md5DigestAsHex(userOutput.getBytes());
        if (userOutputMd5.equals(testcaseInfo.getStr("outputMd5"))) {
            return JudgeStatus.STATUS_ACCEPTED.getStatus();
        }
        // 如果不AC, 进行PE判断, 否则为WA
        userOutputMd5 = DigestUtils.md5DigestAsHex(userOutput.replaceAll("\\s+", "").getBytes());
        if (userOutputMd5.equals(testcaseInfo.getStr("allStrippedOutputMd5"))) {
            return JudgeStatus.STATUS_PRESENTATION_ERROR.getStatus();
        }
        return JudgeStatus.STATUS_WRONG_ANSWER.getStatus();
    }


    /**
     * 去除行末尾空白符
     *
     * @param value
     * @return
     */
    private String rtrim(String value) {
        if (value == null)
            return null;
        return EOL_PATTERN.matcher(StrUtil.trimEnd(value)).replaceAll("");
    }

}