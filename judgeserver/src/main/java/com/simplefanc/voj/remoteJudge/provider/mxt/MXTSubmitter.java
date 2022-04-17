package com.simplefanc.voj.remoteJudge.provider.mxt;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.remoteJudge.RemoteOjInfo;
import com.simplefanc.voj.remoteJudge.SubmissionInfo;
import com.simplefanc.voj.remoteJudge.account.RemoteAccount;
import com.simplefanc.voj.remoteJudge.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.remoteJudge.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.remoteJudge.httpclient.HttpStatusValidator;
import com.simplefanc.voj.remoteJudge.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.remoteJudge.submitter.Submitter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MXTSubmitter implements Submitter {
    private static final Map<String, String> LANGUAGE_MAP = new HashMap<String, String>() {{
        put("C", "0");
        put("C++", "1");
        put("Pascal", "2");
        put("Java", "3");
        put("Ruby", "4");
        put("Bash", "5");
        put("Python", "6");
        put("PHP", "7");
        put("Perl", "8");
        put("C#", "9");
        put("Obj-C", "10");
        put("FreeBasic", "11");
        put("Scheme", "12");
        put("Clang", "13");
        put("Clang++", "14");
        put("Lua", "15");
        put("JavaScript", "16");
        put("Go", "17");
        put("SQL(sqlite3)", "18");
        put("Fortran", "19");
        put("Matlab(Octave)", "20");
    }};
    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return MXTInfo.INFO;
    }

    protected String getMaxRunId(SubmissionInfo info, DedicatedHttpClient client) throws Exception {
        return info.remoteRunId;
    }

    @Override
    public void submit(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "language", LANGUAGE_MAP.get(info.language),
                "notes_id", "0",
                "source", info.userCode
        );
        HttpPost post = new HttpPost("/submit/7/" + info.remotePid + "/");
        post.setEntity(entity);
        String result = client.execute(post, HttpStatusValidator.SC_OK).getBody();
        final JSONObject jsonObject = JSONUtil.parseObj(result);
        info.remoteRunId = jsonObject.getStr("solution_id");
    }
}
