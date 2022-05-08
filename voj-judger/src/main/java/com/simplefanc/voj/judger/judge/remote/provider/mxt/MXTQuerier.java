package com.simplefanc.voj.judger.judge.remote.provider.mxt;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.judge.remote.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.SubmissionRemoteStatus;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.querier.Querier;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MXTQuerier implements Querier {

    private static final Map<Integer, JudgeStatus> statusMap = new HashMap<Integer, JudgeStatus>() {
        {
            put(0, JudgeStatus.STATUS_PENDING);
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
            put(14, JudgeStatus.STATUS_JUDGING);
        }
    };

    private static final String[] statusArray = new String[]{
            // 0
            "Pending",
            // 1
            "Pending Rejudging",
            // 2
            "Compiling",
            // 3
            "Running & Judging",
            // 4
            "Accepted",
            // 5
            "Presentation Error",
            // 6
            "Wrong Answer",
            // 7
            "Time Limit Exceed",
            // 8
            "Memory Limit Exceed",
            // 9
            "Output Limit Exceed",
            // 10
            "Runtime Error",
            // 11
            "Compile Error",
            // 12
            "Compile OK",
            // 13
            "Test Running Done",
            // 14
            "Read"};

    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return MXTInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        HttpPost post = new HttpPost("/submit/solution/" + info.remoteRunId + "/");
        String body = client.execute(post, HttpStatusValidator.SC_OK).getBody();
        final JSONObject jsonObject = JSONUtil.parseObj(body);
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        Integer result = jsonObject.getInt("result");
        status.rawStatus = statusArray[result];
        // 3ms
        status.executionTime = jsonObject.getInt("time");
        // 1156B = 1.13KB
        status.executionMemory = jsonObject.getInt("memory") / 1024;
        // 从原生状态映射到统一状态
        status.statusType = statusMap.get(result);
        if (status.statusType == JudgeStatus.STATUS_COMPILE_ERROR) {
            String compileinfo = client.post("/compileinfo/" + info.remoteRunId + "/").getBody();
            status.compilationErrorInfo = JSONUtil.parseObj(compileinfo).getStr("error");
        }
        status.failCase = (int) (Double.parseDouble(jsonObject.getStr("pass_rate")) * 100);
        return status;
    }

}
