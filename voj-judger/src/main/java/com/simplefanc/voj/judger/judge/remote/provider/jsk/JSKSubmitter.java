package com.simplefanc.voj.judger.judge.remote.provider.jsk;

import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.*;
import com.simplefanc.voj.judger.judge.remote.submitter.Submitter;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JSKSubmitter implements Submitter {

    private static final Map<String, String> FILE_MAP = new HashMap<>() {
        {
            put("FAILED", "Failed");
            put("c", "main.c");
            put("c_noi", "main.c");
            put("c++", "main.cpp");
            put("c++14", "main.cpp");
            put("c++_noi", "main.cpp");
            put("java", "Main.java");
            put("python", "main.py");
            put("python3", "main.py");
            put("ruby", "main.rb");
            put("blockly", "main.bl");
            put("octave", "main.m");
            put("pascal", "main.pas");
            put("go", "main.go");
        }
    };

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return JSKInfo.INFO;
    }

    @Override
    public void submit(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        HttpEntity entity = SimpleNameValueEntityFactory.create("codes[]", info.userCode, "file[]",
                FILE_MAP.get(info.language), "id", info.remotePid, "language", info.language);
        HttpPost post = new HttpPost("/solve/submit");
        post.setEntity(entity);
        post.setHeader("X-XSRF-TOKEN", CookieUtil.getCookieValue(client, "XSRF-TOKEN"));
        String result = client.execute(post, HttpStatusValidator.SC_OK).getBody();
        info.remoteRunId = JSONUtil.parseObj(result).getStr("data");
    }

}
