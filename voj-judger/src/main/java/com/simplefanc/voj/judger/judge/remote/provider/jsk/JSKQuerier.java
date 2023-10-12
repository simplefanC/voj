package com.simplefanc.voj.judger.judge.remote.provider.jsk;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionRemoteStatus;
import com.simplefanc.voj.judger.judge.remote.querier.Querier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class JSKQuerier implements Querier {

    /**
     * 统计计蒜客的所有静态原生状态 --> unkown error 暂时未统计
     */
    private static final Map<String, JudgeStatus> STATUS_MAP = new HashMap<>() {
        {
            put("AC", JudgeStatus.STATUS_ACCEPTED);
            put("PE", JudgeStatus.STATUS_PRESENTATION_ERROR);
            put("WA", JudgeStatus.STATUS_WRONG_ANSWER);
            put("TL", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED);
            put("ML", JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("OL", JudgeStatus.STATUS_OUTPUT_LIMIT_EXCEEDED);
            put("RE", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("RE_SEGV", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("RE_ABRT", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("RE_FPE", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("RE_SYS", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("SF", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("AE", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("CE", JudgeStatus.STATUS_COMPILE_ERROR);
            put("pending", JudgeStatus.STATUS_JUDGING);
            put("fail", JudgeStatus.STATUS_SUBMITTED_FAILED);
        }
    };

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return JSKInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        // 执行时间和内存
        HttpGet get = new HttpGet("/api/problem/solve/result?solveResultKey=" + info.remoteRunId);
        String result = client.execute(get, HttpStatusValidator.SC_OK).getBody();
        JSONObject data = JSONUtil.parseObj(result);
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = data.getStr("status");
        status.statusType = STATUS_MAP.computeIfAbsent(status.rawStatus, k -> {
            if (k.startsWith("RE")) {
                return JudgeStatus.STATUS_RUNTIME_ERROR;
            }
            return JudgeStatus.STATUS_JUDGING;
        });
//        status.statusType = STATUS_MAP.getOrDefault(status.rawStatus, JudgeStatus.STATUS_JUDGING);
        if (status.statusType != JudgeStatus.STATUS_JUDGING) {
            status.executionMemory = data.getInt("usedMemory");
            status.executionTime = data.getInt("usedTime");
        }
        if (status.statusType == JudgeStatus.STATUS_COMPILE_ERROR) {
            // 编译错误信息
            status.compilationErrorInfo = data.getStr("compileError");
        }
        return status;
    }

}
