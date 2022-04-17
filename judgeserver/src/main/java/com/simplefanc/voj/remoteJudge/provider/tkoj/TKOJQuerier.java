package com.simplefanc.voj.remoteJudge.provider.tkoj;

import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.remoteJudge.RemoteOjInfo;
import com.simplefanc.voj.remoteJudge.SubmissionInfo;
import com.simplefanc.voj.remoteJudge.SubmissionRemoteStatus;
import com.simplefanc.voj.remoteJudge.account.RemoteAccount;
import com.simplefanc.voj.remoteJudge.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.remoteJudge.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.remoteJudge.httpclient.HttpStatusValidator;
import com.simplefanc.voj.remoteJudge.querier.Querier;
import com.simplefanc.voj.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TKOJQuerier implements Querier {
    private static final String[] statusArray = new String[]{
            "Pending",
            "Pending Rejudging",
            "Compiling",
            "Running Judging",
            "Accepted",
            "Presentation Error",
            "Wrong Answer",
            "Time Limit Exceed",
            "Memory Limit Exceed",
            "Output Limit Exceed",
            "Runtime Error",
            "Compile Error",
            "Compile OK",
            "Runtime Finish"
    };
    private static final Map<Integer, Constants.Judge> statusMap = new HashMap<Integer, Constants.Judge>() {{
        put(0, Constants.Judge.STATUS_PENDING);
        put(1, Constants.Judge.STATUS_JUDGING);
        put(2, Constants.Judge.STATUS_COMPILING);
        put(3, Constants.Judge.STATUS_JUDGING);
        put(4, Constants.Judge.STATUS_ACCEPTED);
        put(5, Constants.Judge.STATUS_PRESENTATION_ERROR);
        put(6, Constants.Judge.STATUS_WRONG_ANSWER);
        put(7, Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
        put(8, Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
        put(9, Constants.Judge.STATUS_OUTPUT_LIMIT_EXCEEDED);
        put(10, Constants.Judge.STATUS_RUNTIME_ERROR);
        put(11, Constants.Judge.STATUS_COMPILE_ERROR);
        put(12, Constants.Judge.STATUS_JUDGING);
        put(13, Constants.Judge.STATUS_JUDGING);
    }};
    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return TKOJInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        String result = client.get("/status-ajax.php?solution_id=" + info.remoteRunId, HttpStatusValidator.SC_OK).getBody();
        String[] results = result.split(",");
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = statusArray[Integer.parseInt(results[0])];
        status.statusType = statusMap.get(Integer.parseInt(results[0]));
        status.executionMemory = Integer.parseInt(results[1]);
        status.executionTime = Integer.parseInt(results[2]);

        if (status.statusType == Constants.Judge.STATUS_COMPILE_ERROR) {
            String ceinfo = client.get("/ceinfo.php?sid=" + info.remoteRunId).getBody();
            status.compilationErrorInfo = "<pre>" + ReUtil.getGroup1("id='errtxt'\\s*?>([\\s\\S]*?</pre>)", ceinfo);
        }
        return status;
    }
}
