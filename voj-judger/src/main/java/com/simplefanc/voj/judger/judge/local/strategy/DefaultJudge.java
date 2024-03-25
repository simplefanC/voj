package com.simplefanc.voj.judger.judge.local.strategy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.common.constants.RunConfig;
import com.simplefanc.voj.judger.common.exception.SystemException;
import com.simplefanc.voj.judger.judge.local.SandboxRun;
import com.simplefanc.voj.judger.judge.local.pojo.CaseResult;
import com.simplefanc.voj.judger.judge.local.pojo.JudgeCaseDTO;
import com.simplefanc.voj.judger.judge.local.pojo.JudgeGlobalDTO;
import com.simplefanc.voj.judger.judge.local.pojo.SandBoxRes;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

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
    public JSONArray judgeCase(JudgeCaseDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemException {
        RunConfig runConfig = judgeGlobalDTO.getRunConfig();
        // 调用安全沙箱使用测试点对程序进行测试
        return SandboxRun.testCase(
                parseRunCommand(runConfig, null, null, null),
                runConfig.getEnvs(),
                judgeDTO.getTestCaseInputPath(),
                judgeGlobalDTO.getTestTime(),
                judgeGlobalDTO.getMaxMemory(),
                judgeDTO.getMaxOutputSize(),
                judgeGlobalDTO.getMaxStack(),
                runConfig.getExeName(),
                judgeGlobalDTO.getUserFileId(),
                judgeGlobalDTO.getUserFileSrc());
    }

    @Override
    public CaseResult processResult(SandBoxRes sandBoxRes, JudgeCaseDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) {
        CaseResult result = new CaseResult();
        // 如果测试跑题无异常
        if (sandBoxRes.getStatus().equals(JudgeStatus.STATUS_ACCEPTED.getStatus())) {
            // 比对题目限制、测试数据
            success(result, sandBoxRes, judgeDTO, judgeGlobalDTO);
        } else if (sandBoxRes.getStatus().equals(JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus())) {
            result.setStatus(JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (sandBoxRes.getExitCode() != 0) {
            // STATUS_RUNTIME_ERROR 记录errMsg
            abort(result, sandBoxRes);
        } else {
            result.setStatus(sandBoxRes.getStatus());
        }

        result.setMemory(sandBoxRes.getMemory());
        result.setTime(sandBoxRes.getTime());

        // 如果需要获取用户对于该题目的输出
        if (judgeGlobalDTO.getNeedUserOutputFile()) {
            result.setOutput(sandBoxRes.getStdout());
        }
        return result;
    }

    private void abort(CaseResult result, SandBoxRes sandBoxRes) {
        result.setStatus(JudgeStatus.STATUS_RUNTIME_ERROR.getStatus());
        String errMsg;
        if (sandBoxRes.getExitCode() < 32) {
            errMsg = String.format("The program return exit status code: %s (%s)\n", sandBoxRes.getExitCode(),
                    SandboxRun.SIGNALS.get(sandBoxRes.getExitCode()));
        } else {
            errMsg = String.format("The program return exit status code: %s\n", sandBoxRes.getExitCode());
        }
        result.setErrMsg(errMsg.substring(0, Math.min(1024 * 1024, errMsg.length())));
    }


    private void success(CaseResult result, SandBoxRes sandBoxRes, JudgeCaseDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) {
        // 对结果的时间损耗和空间损耗与题目限制做比较，判断是否mle和tle
        if (sandBoxRes.getTime() > judgeGlobalDTO.getMaxTime()) {
            result.setStatus(JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (sandBoxRes.getMemory() > judgeGlobalDTO.getMaxMemory() * 1024) {
            result.setStatus(JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
        } else {
            // 与原测试数据输出的md5进行对比 AC或者是WA
            JSONObject testcaseInfo = (JSONObject) ((JSONArray) judgeGlobalDTO.getTestCaseInfo().get("testCases"))
                    .get(judgeDTO.getTestCaseNum() - 1);
            result.setStatus(compareOutput(sandBoxRes.getStdout(), judgeGlobalDTO.getRemoveEOLBlank(), testcaseInfo));
        }
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
        if (value == null) {
            return null;
        }
        return EOL_PATTERN.matcher(StrUtil.trimEnd(value)).replaceAll("");
    }

}