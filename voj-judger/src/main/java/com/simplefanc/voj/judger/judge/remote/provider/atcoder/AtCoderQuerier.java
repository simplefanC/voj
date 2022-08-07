package com.simplefanc.voj.judger.judge.remote.provider.atcoder;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionRemoteStatus;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.querier.Querier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AtCoderQuerier implements Querier {

    public static final String SUBMISSION_RESULT_URL = "/contests/%s/submissions/%s";

    private static final Map<String, JudgeStatus> STATUS_MAP = new HashMap<>() {{
        put("CE", JudgeStatus.STATUS_COMPILE_ERROR);
        put("RE", JudgeStatus.STATUS_RUNTIME_ERROR);
        put("QLE", JudgeStatus.STATUS_RUNTIME_ERROR);
        put("OLE", JudgeStatus.STATUS_RUNTIME_ERROR);
        put("IE", JudgeStatus.STATUS_RUNTIME_ERROR);
        put("WA", JudgeStatus.STATUS_WRONG_ANSWER);
        put("AC", JudgeStatus.STATUS_ACCEPTED);
        put("TLE", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED);
        put("MLE", JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED);
        put("WJ", JudgeStatus.STATUS_JUDGING);
        put("WR", JudgeStatus.STATUS_JUDGING); // Waiting Rejudge
        put("Judging", JudgeStatus.STATUS_JUDGING); // Waiting Rejudge
    }};

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return AtCoderInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        String url = String.format(SUBMISSION_RESULT_URL, info.remoteContestId, info.remoteRunId);
        String body = client.get(url).getBody();
        status.rawStatus = ReUtil.get("<th>Status</th>[\\s\\S]*?<td id=\"judge-status\" class=\"[\\s\\S]*?\"><span [\\s\\S]*?>([\\s\\S]*?)</span></td>", body, 1);
        status.statusType = STATUS_MAP.getOrDefault(status.rawStatus, JudgeStatus.STATUS_JUDGING);
        if (status.statusType == JudgeStatus.STATUS_JUDGING) {
            return status;
        }
        if (status.statusType == JudgeStatus.STATUS_COMPILE_ERROR) {
            String ceInfo = ReUtil.get("<h4>Compile Error</h4>[\\s\\S]*?<pre>([\\s\\S]*?)</pre>", body, 1);
            status.compilationErrorInfo = HtmlUtil.unescape(ceInfo);
            return status;
        }
        String time = ReUtil.get("<th>Exec Time</th>[\\s\\S]*?<td [\\s\\S]*?>([\\s\\S]*?) ms</td>", body, 1);
        String memory = ReUtil.get("<th>Memory</th>[\\s\\S]*?<td [\\s\\S]*?>([\\s\\S]*?) KB</td>", body, 1);
        status.executionTime = time == null ? 0 : Integer.parseInt(time);
        status.executionMemory = memory == null ? 0 : Integer.parseInt(memory);
        return status;
    }

}
