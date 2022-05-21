package com.simplefanc.voj.judger.judge.remote.provider.tkoj;

import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.judger.judge.remote.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.httpclient.SimpleNameValueEntityFactory;
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
public class TKOJSubmitter implements Submitter {

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
        }
    };

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return TKOJInfo.INFO;
    }

    protected String getRunId(SubmissionInfo info, DedicatedHttpClient client) {
        String html = client.get("/status.php?problem_id=" + info.remotePid + "&user_id=" + info.remoteAccountId,
                HttpStatusValidator.SC_OK).getBody();
        return ReUtil.getGroup1("prevtop=(\\d+)", html);
    }

    @Override
    public void submit(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        // 进行代码提交
        HttpEntity entity = SimpleNameValueEntityFactory.create("id", info.remotePid, "language", info.language,
                "source", info.userCode, "csrf", TKOJVerifyUtil.getCsrf(client), "vcode",
                TKOJVerifyUtil.getCaptcha(client));
        HttpPost post = new HttpPost("/submit.php");
        post.setEntity(entity);
        client.execute(post, HttpStatusValidator.SC_MOVED_TEMPORARILY);
        if ((info.remoteRunId = getRunId(info, client)) == null) {
            // 等待2s再次查询，如果还是失败，则表明提交失败了
            TimeUnit.SECONDS.sleep(2);
            info.remoteRunId = getRunId(info, client);
        }
    }

}
