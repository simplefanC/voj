package com.simplefanc.voj.judger.judge.remote.provider.jsk;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.*;
import com.simplefanc.voj.judger.judge.remote.loginer.RetentiveLoginer;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JSKLoginer extends RetentiveLoginer {

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return JSKInfo.INFO;
    }

    @Override
    protected void loginEnforce(RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        client.get("/");

        HttpPost post = new HttpPost("/api/auth/sign-in");
        JSONObject json = new JSONObject();
        json.set("account", account.accountId);
        json.set("password", SecureUtil.md5(account.password));
        json.set("verification", "");
        json.set("keepOnline", true);
        post.setEntity(new StringEntity(json.toString(), StandardCharsets.UTF_8));
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        post.setHeader("X-XSRF-TOKEN", CookieUtil.getCookieValue(client, "XSRF-TOKEN"));
        client.execute(post, HttpStatusValidator.SC_OK);
    }

}
