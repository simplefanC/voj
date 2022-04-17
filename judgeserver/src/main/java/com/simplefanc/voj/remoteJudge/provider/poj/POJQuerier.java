package com.simplefanc.voj.remoteJudge.provider.poj;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.remoteJudge.RemoteOjInfo;
import com.simplefanc.voj.remoteJudge.SubmissionInfo;
import com.simplefanc.voj.remoteJudge.SubmissionRemoteStatus;
import com.simplefanc.voj.remoteJudge.account.RemoteAccount;
import com.simplefanc.voj.remoteJudge.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.remoteJudge.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.remoteJudge.httpclient.HttpBodyValidator;
import com.simplefanc.voj.remoteJudge.querier.Querier;
import com.simplefanc.voj.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class POJQuerier implements Querier {
    private static final Map<String, Constants.Judge> statusMap = new HashMap<String, Constants.Judge>() {
        {
            put("Compiling", Constants.Judge.STATUS_COMPILING);
            put("Accepted", Constants.Judge.STATUS_ACCEPTED);
            put("Running & Judging", Constants.Judge.STATUS_JUDGING);
            put("Presentation Error", Constants.Judge.STATUS_PRESENTATION_ERROR);
            put("Time Limit Exceeded", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
            put("Memory Limit Exceeded", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("Wrong Answer", Constants.Judge.STATUS_WRONG_ANSWER);
            put("Runtime Error", Constants.Judge.STATUS_RUNTIME_ERROR);
            put("Output Limit Exceeded", Constants.Judge.STATUS_OUTPUT_LIMIT_EXCEEDED);
            put("Compile Error", Constants.Judge.STATUS_COMPILE_ERROR);
        }
    };
    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return POJInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        // 需要登录
        String html = client.get("/showsource?solution_id=" + info.remoteRunId,
                new HttpBodyValidator("<title>Error</title>", true)).getBody();

        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = ReUtil.getGroup1("<b>Result:</b>(.+?)</td>", html).replaceAll("<.*?>", "").trim();
        status.statusType = statusMap.get(status.rawStatus);
        if (status.statusType == Constants.Judge.STATUS_ACCEPTED) {
            status.executionMemory = Integer.parseInt(ReUtil.getGroup1("<b>Memory:</b> ([-\\d]+)", html));
            status.executionTime = Integer.parseInt(ReUtil.getGroup1("<b>Time:</b> ([-\\d]+)", html));
        } else if (status.statusType == Constants.Judge.STATUS_COMPILE_ERROR) {
            html = client.get("/showcompileinfo?solution_id=" + info.remoteRunId).getBody();
            Assert.isTrue(html.contains("Compile Error"));
            status.compilationErrorInfo = ReUtil.getGroup1("(<pre>[\\s\\S]*?</pre>)", html);
        }
        return status;
    }
}
