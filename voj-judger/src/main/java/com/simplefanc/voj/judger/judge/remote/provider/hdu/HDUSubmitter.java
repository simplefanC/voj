package com.simplefanc.voj.judger.judge.remote.provider.hdu;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.*;
import com.simplefanc.voj.judger.judge.remote.submitter.Submitter;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class HDUSubmitter implements Submitter {
    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return HDUInfo.INFO;
    }

    protected String getMaxRunId(SubmissionInfo info, DedicatedHttpClient client) {
        String html = client.get("/status.php?user=" + info.remoteAccountId + "&pid=" + info.remotePid).getBody();
        return ReUtil.get("<td height=22px>(\\d+)", html, 1);
    }

    @Override
    public void submit(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "_usercode", Base64.encode(URLEncoder.encode(info.userCode, "utf-8").getBytes("utf-8")),
                "check", "0",
                "language", LANGUAGE_MAP.get(info.language),
                "problemid", info.remotePid
        );
        final HttpPost post = new HttpPost("/submit.php?action=submit");
        post.setEntity(entity);
        SimpleHttpResponse response = client.execute(post);
        // 提交频率限制了 等待5秒再次提交
        if (response.getStatusCode() == HttpStatus.SC_OK && response.getBody() != null && response.getBody().contains("Please don't re-submit")) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.execute(post, HttpStatusValidator.SC_MOVED_TEMPORARILY);
        } else if (response.getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
            String log = String.format("[HDU] [%s]: Failed to submit code, the http response status is [%s].", info.remotePid, response.getStatusCode());
            throw new RuntimeException(log);
        }
        info.remoteRunId = getMaxRunId(info, client);
        // 等待2s再次查询，如果还是失败，则表明提交失败了
        if (StrUtil.isEmpty(info.remoteRunId)) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            info.remoteRunId = getMaxRunId(info, client);
        }
    }

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>() {
        {
            put("G++", "0");
            put("GCC", "1");
            put("C++", "2");
            put("C", "3");
            put("Pascal", "4");
            put("Java", "5");
            put("C#", "6");
        }
    };
}