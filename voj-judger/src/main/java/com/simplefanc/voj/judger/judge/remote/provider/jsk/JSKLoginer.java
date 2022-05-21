package com.simplefanc.voj.judger.judge.remote.provider.jsk;

import cn.hutool.crypto.SecureUtil;
import com.simplefanc.voj.judger.judge.remote.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.*;
import com.simplefanc.voj.judger.judge.remote.loginer.RetentiveLoginer;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JSKLoginer extends RetentiveLoginer {

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return JSKInfo.LOGIN;
    }

    @Override
    protected void loginEnforce(RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        if (client.get("/?n=https://nanti.jisuanke.com/oi#/").getBody().contains("i.jisuanke.com/setting/basic")) {
            return;
        }
        HttpEntity entity = SimpleNameValueEntityFactory.create("account", account.accountId, "pwd",
                SecureUtil.md5(account.password), "save", "1");

        HttpPost post = new HttpPost("/auth/login");
        post.setEntity(entity);
        post.setHeader("X-XSRF-TOKEN", CookieUtil.getCookieValue(client, "XSRF-TOKEN"));
        client.execute(post, HttpStatusValidator.SC_OK, new HttpBodyValidator("success"));
    }

}
