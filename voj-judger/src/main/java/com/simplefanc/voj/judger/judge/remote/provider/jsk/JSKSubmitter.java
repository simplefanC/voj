package com.simplefanc.voj.judger.judge.remote.provider.jsk;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.CookieUtil;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.submitter.Submitter;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
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
        HttpPost post = new HttpPost("/api/problem/solve/submit");
        JSONObject json = getJsonObject(info);
        post.setEntity(new StringEntity(json.toString(), StandardCharsets.UTF_8));
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        post.setHeader("X-XSRF-TOKEN", CookieUtil.getCookieValue(client, "XSRF-TOKEN"));
        String result = client.execute(post, HttpStatusValidator.SC_OK).getBody();

        info.remoteRunId = result.replaceAll("\"", "");
    }

    private JSONObject getJsonObject(SubmissionInfo info) {
        JSONObject json = new JSONObject();
        json.set("problemId", Integer.parseInt(info.remotePid));
        json.set("language", info.language);
        final JSONArray files = new JSONArray();
        final JSONObject fileObj = new JSONObject();
        fileObj.set("name", FILE_MAP.get(info.language));
        fileObj.set("code", info.userCode);
        fileObj.set("locks", new JSONArray());
        files.add(fileObj);
        json.set("files", files);
        return json;
    }
}
