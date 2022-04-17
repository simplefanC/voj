package com.simplefanc.voj.remoteJudge.provider.poj;


import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.remoteJudge.RemoteOjInfo;
import com.simplefanc.voj.remoteJudge.SubmissionInfo;
import com.simplefanc.voj.remoteJudge.account.RemoteAccount;
import com.simplefanc.voj.remoteJudge.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.remoteJudge.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.remoteJudge.httpclient.HttpStatusValidator;
import com.simplefanc.voj.remoteJudge.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.remoteJudge.submitter.Submitter;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class POJSubmitter implements Submitter {
    private static final Map<String, String> LANGUAGE_MAP = new HashMap<String, String>() {{
        put("G++", "0");
        put("GCC", "1");
        put("Java", "2");
        put("Pascal", "3");
        put("C++", "4");
        put("C", "5");
        put("Fortran", "6");
    }};
    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return POJInfo.INFO;
    }

    protected String getRunId(SubmissionInfo info, DedicatedHttpClient client) {
        String html = client.get("/status?user_id=" + info.remoteAccountId + "&problem_id=" + info.remotePid).getBody();
        return ReUtil.getGroup1("<tr align=center><td>(\\d+)", html);
    }

    @Override
    public void submit(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "language", LANGUAGE_MAP.get(info.language),
                "problem_id", info.remotePid,
                "source", new String(Base64.encodeBase64(info.userCode.getBytes())),
                "encoded", "1",
                "submit", "Submit"
        );
        client.post("/submit", entity, HttpStatusValidator.SC_MOVED_TEMPORARILY);
        if ((info.remoteRunId = getRunId(info, client)) == null) {
            // 等待2s再次查询，如果还是失败，则表明提交失败了
            TimeUnit.SECONDS.sleep(2);
            info.remoteRunId = getRunId(info, client);
        }
    }
}
