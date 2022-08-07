package com.simplefanc.voj.judger.judge.remote.provider.codefores;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.JudgeCase;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionRemoteStatus;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.judger.judge.remote.querier.Querier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class CFQuerier implements Querier {
    public static final String CE_INFO_URL = "/data/submitSource";

    private static final Map<String, JudgeStatus> STATUS_MAP = new HashMap<>() {
        {
            put("FAILED", JudgeStatus.STATUS_SUBMITTED_FAILED);
            put("OK", JudgeStatus.STATUS_ACCEPTED);
            put("PARTIAL", JudgeStatus.STATUS_PARTIAL_ACCEPTED);
            put("COMPILATION_ERROR", JudgeStatus.STATUS_COMPILE_ERROR);
            put("RUNTIME_ERROR", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("WRONG_ANSWER", JudgeStatus.STATUS_WRONG_ANSWER);
            put("PRESENTATION_ERROR", JudgeStatus.STATUS_PRESENTATION_ERROR);
            put("TIME_LIMIT_EXCEEDED", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED);
            put("MEMORY_LIMIT_EXCEEDED", JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("IDLENESS_LIMIT_EXCEEDED", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("SECURITY_VIOLATED", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("CRASHED", JudgeStatus.STATUS_SYSTEM_ERROR);
            put("INPUT_PREPARATION_CRASHED", JudgeStatus.STATUS_SYSTEM_ERROR);
            put("CHALLENGED", JudgeStatus.STATUS_SYSTEM_ERROR);
            put("SKIPPED", JudgeStatus.STATUS_SYSTEM_ERROR);
            put("TESTING", JudgeStatus.STATUS_JUDGING);
            put("REJECTED", JudgeStatus.STATUS_SYSTEM_ERROR);
            put("RUNNING & JUDGING", JudgeStatus.STATUS_JUDGING);
        }
    };

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return CFInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "csrf_token", account.getCsrfToken(),
                "submissionId", info.remoteRunId
        );
        HttpPost post = new HttpPost(CE_INFO_URL);
        post.setEntity(entity);
        String body = client.execute(post, HttpStatusValidator.SC_OK).getBody();
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();

        JSONObject submissionInfoJson = JSONUtil.parseObj(body);
        String compilationError = submissionInfoJson.getStr("compilationError");
        if ("true".equals(compilationError)) {
            status.executionMemory = 0;
            status.executionTime = 0;
            status.statusType = JudgeStatus.STATUS_COMPILE_ERROR;
            String ceMsg = submissionInfoJson.getStr("checkerStdoutAndStderr#1");
            if (StrUtil.isEmpty(ceMsg)) {
                status.compilationErrorInfo = "Oops! Because Codeforces does not provide compilation details, it is unable to provide the reason for compilation failure!";
            } else {
                status.compilationErrorInfo = ceMsg;
            }
            return status;
        }
        if ("true".equals(submissionInfoJson.getStr("waiting"))) {
            status.statusType = JudgeStatus.STATUS_JUDGING;
            return status;
        }

        int maxTime = 0;
        int maxMemory = 0;
        List<JudgeCase> judgeCaseList = new ArrayList<>();
        int testCount = Integer.parseInt(submissionInfoJson.getStr("testCount"));
        for (int testcaseNum = 1; testcaseNum <= testCount; testcaseNum++) {
            String verdict = submissionInfoJson.getStr("verdict#" + testcaseNum);
            if (StrUtil.isEmpty(verdict)) {
                continue;
            }
            JudgeStatus judgeRes = STATUS_MAP.get(verdict);
            int time = Integer.parseInt(submissionInfoJson.getStr("timeConsumed#" + testcaseNum));
            int memory = Integer.parseInt(submissionInfoJson.getStr("memoryConsumed#" + testcaseNum)) / 1024;
            String msg = submissionInfoJson.getStr("checkerStdoutAndStderr#" + testcaseNum);

            judgeCaseList.add(new JudgeCase()
                    .setSubmitId(info.submitId)
                    .setPid(info.pid)
                    .setUid(info.uid)
                    .setTime(time)
                    .setMemory(memory)
                    .setStatus(judgeRes.getStatus())
                    .setUserOutput(msg));
            maxTime = Math.max(maxTime, time);
            maxMemory = Math.max(maxMemory, memory);
        }

        status.executionMemory = maxMemory;
        status.executionTime = maxTime;
        status.judgeCaseList = judgeCaseList;
        status.statusType = STATUS_MAP.get(submissionInfoJson.getStr("verdict#" + testCount));

        return status;
    }

}
