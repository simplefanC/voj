package com.simplefanc.voj.judger.judge.remote.provider.codeforcesgym;

import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionRemoteStatus;
import com.simplefanc.voj.judger.judge.remote.provider.shared.codeforces.AbstractCFStyleQuerier;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GYMQuerier extends AbstractCFStyleQuerier {
    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;
    private static final String SUBMISSION_BY_USERNAME = "/submissions/%s";
    private static final String JUDGE_PROTOCOL = "/data/judgeProtocol";

    @Override
    public RemoteOjInfo getOjInfo() {
        return GYMInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());

        String body = client.get(String.format(SUBMISSION_BY_USERNAME, account.accountId)).getBody();
        String regex = "<span .*? submissionId=\"" + info.remoteRunId + "\" submissionVerdict=\"(.*?)\" .*?>.*?</span>.*?<i .*?></i>[\\s]*?</td>[\\s]*?" +
                "<td class=\"time.*?\">[\\s]*?(\\d+)&nbsp;ms[\\s]*?</td>[\\s]*?" +
                "<td class=\"memory.*?\">[\\s]*?(\\d+)&nbsp;KB[\\s]*?</td>[\\s]*?</tr>";
        Pattern pattern = PatternPool.get(regex, Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(body);
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.statusType = JudgeStatus.STATUS_JUDGING;
        if (matcher.find()) {
            String statusStr = matcher.group(1);
            status.statusType = STATUS_MAP.getOrDefault(statusStr, JudgeStatus.STATUS_JUDGING);
            if (status.statusType == JudgeStatus.STATUS_JUDGING) {
                return status;
            }
            String timeStr = matcher.group(2);
            status.executionTime = StrUtil.isEmpty(timeStr) ? 0 : Integer.parseInt(timeStr);
            String memoryStr = matcher.group(3);
            status.executionMemory = StrUtil.isEmpty(memoryStr) ? 0 : Integer.parseInt(memoryStr);
            if (status.statusType == JudgeStatus.STATUS_COMPILE_ERROR) {
                HttpEntity entity = SimpleNameValueEntityFactory.create(
                        "csrf_token", account.getCsrfToken(),
                        "submissionId", info.remoteRunId
                );
                HttpPost post = new HttpPost(JUDGE_PROTOCOL);
                post.setEntity(entity);
                String ceInfo = client.execute(post, HttpStatusValidator.SC_OK).getBody();
                status.compilationErrorInfo = UnicodeUtil.toString(ceInfo).replaceAll("(\\\\r)?\\\\n", "\n")
                        .replaceAll("\\\\\\\\", "\\\\");
            }
        }

        return status;
    }

}
