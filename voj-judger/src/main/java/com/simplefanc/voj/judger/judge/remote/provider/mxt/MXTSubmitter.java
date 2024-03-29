package com.simplefanc.voj.judger.judge.remote.provider.mxt;

import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.submitter.Submitter;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MXTSubmitter implements Submitter {

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<String, String>() {
        {
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
        }
    };

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return MXTInfo.INFO;
    }

    @Override
    public void submit(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        HttpEntity entity = SimpleNameValueEntityFactory.create("language", LANGUAGE_MAP.get(info.language), "notes_id",
                "0", "source", info.userCode);
        HttpPost post = new HttpPost("/submit/7/" + info.remotePid + "/");
        post.setEntity(entity);
        info.remoteRunId = getRunId(client, post);
    }

    private String getRunId(DedicatedHttpClient client, HttpPost post) throws InterruptedException {
        String result = client.execute(post, HttpStatusValidator.SC_OK).getBody();
        JSONObject jsonObject = JSONUtil.parseObj(result);
        if (!jsonObject.getBool("success")) {
            int interval = Integer.parseInt(ReUtil.getGroup1("(\\d+)秒", jsonObject.getStr("msg")));
            TimeUnit.SECONDS.sleep(interval);
            result = client.execute(post, HttpStatusValidator.SC_OK).getBody();
            jsonObject = JSONUtil.parseObj(result);
        }
        return jsonObject.getStr("solution_id");
    }

}
