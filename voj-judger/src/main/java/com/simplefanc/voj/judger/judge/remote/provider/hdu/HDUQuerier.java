package com.simplefanc.voj.judger.judge.remote.provider.hdu;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class HDUQuerier implements Querier {
    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return HDUInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, null, getOjInfo().defaultChaset);

        String html = client.get("/status.php?first=" + info.remoteRunId).getBody();
        Pattern pattern = Pattern.compile(">" + info.remoteRunId + "</td><td>[\\s\\S]*?</td><td>([\\s\\S]*?)</td><td>[\\s\\S]*?</td><td>(\\d*?)MS</td><td>(\\d*?)K</td>");
        Matcher matcher = pattern.matcher(html);
        Assert.isTrue(matcher.find());

        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = matcher.group(1).replaceAll("<[\\s\\S]*?>", "").trim();
        status.statusType = STATUS_MAP.getOrDefault(status.rawStatus, JudgeStatus.STATUS_JUDGING);
        if (status.statusType == JudgeStatus.STATUS_JUDGING) {
            return status;
        }
        if (status.statusType == JudgeStatus.STATUS_ACCEPTED) {
            status.executionTime = Integer.parseInt(matcher.group(2));
            status.executionMemory = Integer.parseInt(matcher.group(3));
        } else if (status.statusType == JudgeStatus.STATUS_COMPILE_ERROR) {
            html = client.get("/viewerror.php?rid=" + info.remoteRunId).getBody();
            status.compilationErrorInfo = ReUtil.get("<pre>([\\s\\S]*?)</pre>", html, 1);
        }
        return status;
    }

    private static final Map<String, JudgeStatus> STATUS_MAP = new HashMap<>() {
        {
            put("Submitted", JudgeStatus.STATUS_JUDGING);
            put("Accepted", JudgeStatus.STATUS_ACCEPTED);
            put("Wrong Answer", JudgeStatus.STATUS_WRONG_ANSWER);
            put("Compilation Error", JudgeStatus.STATUS_COMPILE_ERROR);
            put("Queuing", JudgeStatus.STATUS_JUDGING);
            put("Running", JudgeStatus.STATUS_JUDGING);
            put("Compiling", JudgeStatus.STATUS_COMPILING);
            put("Runtime Error", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("Time Limit Exceeded", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED);
            put("Memory Limit Exceeded", JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("Output Limit Exceeded", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("Presentation Error", JudgeStatus.STATUS_PRESENTATION_ERROR);
        }
    };
}