package com.simplefanc.voj.judger.judge.remote.provider.poj;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.judge.remote.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.SubmissionRemoteStatus;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpBodyValidator;
import com.simplefanc.voj.judger.judge.remote.querier.Querier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class POJQuerier implements Querier {

    private static final Map<String, JudgeStatus> statusMap = new HashMap<>() {
        {
            put("Compiling", JudgeStatus.STATUS_COMPILING);
            put("Accepted", JudgeStatus.STATUS_ACCEPTED);
            put("Running & Judging", JudgeStatus.STATUS_JUDGING);
            put("Presentation Error", JudgeStatus.STATUS_PRESENTATION_ERROR);
            put("Time Limit Exceeded", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED);
            put("Memory Limit Exceeded", JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("Wrong Answer", JudgeStatus.STATUS_WRONG_ANSWER);
            put("Runtime Error", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("Output Limit Exceeded", JudgeStatus.STATUS_OUTPUT_LIMIT_EXCEEDED);
            put("Compile Error", JudgeStatus.STATUS_COMPILE_ERROR);
        }
    };

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return POJInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        // 需要登录
        String html = client
                .get("/showsource?solution_id=" + info.remoteRunId, new HttpBodyValidator("<title>Error</title>", true))
                .getBody();

        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = ReUtil.getGroup1("<b>Result:</b>(.+?)</td>", html).replaceAll("<.*?>", "").trim();
        status.statusType = statusMap.get(status.rawStatus);
        if (status.statusType == JudgeStatus.STATUS_ACCEPTED) {
            status.executionMemory = Integer.parseInt(ReUtil.getGroup1("<b>Memory:</b> ([-\\d]+)", html));
            status.executionTime = Integer.parseInt(ReUtil.getGroup1("<b>Time:</b> ([-\\d]+)", html));
        } else if (status.statusType == JudgeStatus.STATUS_COMPILE_ERROR) {
            html = client.get("/showcompileinfo?solution_id=" + info.remoteRunId).getBody();
            Assert.isTrue(html.contains("Compile Error"));
            status.compilationErrorInfo = ReUtil.getGroup1("(<pre>[\\s\\S]*?</pre>)", html);
        }
        return status;
    }

}
