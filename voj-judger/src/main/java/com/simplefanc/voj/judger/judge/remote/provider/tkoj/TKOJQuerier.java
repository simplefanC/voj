package com.simplefanc.voj.judger.judge.remote.provider.tkoj;

import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionRemoteStatus;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.querier.Querier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TKOJQuerier implements Querier {

    private static final String[] STATUS_ARRAY = new String[]{"Pending", "Pending Rejudging", "Compiling",
            "Running Judging", "Accepted", "Presentation Error", "Wrong Answer", "Time Limit Exceed",
            "Memory Limit Exceed", "Output Limit Exceed", "Runtime Error", "Compile Error", "Compile OK",
            "Runtime Finish"};

    private static final Map<Integer, JudgeStatus> STATUS_MAP = new HashMap<>() {
        {
            put(0, JudgeStatus.STATUS_JUDGING);
            put(1, JudgeStatus.STATUS_JUDGING);
            put(2, JudgeStatus.STATUS_COMPILING);
            put(3, JudgeStatus.STATUS_JUDGING);
            put(4, JudgeStatus.STATUS_ACCEPTED);
            put(5, JudgeStatus.STATUS_PRESENTATION_ERROR);
            put(6, JudgeStatus.STATUS_WRONG_ANSWER);
            put(7, JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED);
            put(8, JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED);
            put(9, JudgeStatus.STATUS_OUTPUT_LIMIT_EXCEEDED);
            put(10, JudgeStatus.STATUS_RUNTIME_ERROR);
            put(11, JudgeStatus.STATUS_COMPILE_ERROR);
            put(12, JudgeStatus.STATUS_JUDGING);
            put(13, JudgeStatus.STATUS_JUDGING);
        }
    };

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return TKOJInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        String result = client.get("/status-ajax.php?solution_id=" + info.remoteRunId, HttpStatusValidator.SC_OK)
                .getBody();
        String[] results = result.split(",");
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = STATUS_ARRAY[Integer.parseInt(results[0])];
        status.statusType = STATUS_MAP.getOrDefault(Integer.parseInt(results[0]), JudgeStatus.STATUS_JUDGING);
        status.executionMemory = Integer.parseInt(results[1]);
        status.executionTime = Integer.parseInt(results[2]);

        if (status.statusType == JudgeStatus.STATUS_COMPILE_ERROR) {
            String ceinfo = client.get("/ceinfo.php?sid=" + info.remoteRunId).getBody();
            status.compilationErrorInfo = "<pre>" + ReUtil.getGroup1("id='errtxt'\\s*?>([\\s\\S]*?</pre>)", ceinfo);
        }
        return status;
    }

}
