package com.simplefanc.voj.judger.judge.remote.provider.shared.codeforces;

import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.judger.judge.remote.loginer.RetentiveLoginer;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public abstract class CFStyleLoginer extends RetentiveLoginer {

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    protected void loginEnforce(RemoteAccount account) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        final String homePage = client.get("/").getBody();
        if (homePage.contains("/logout\">") && homePage.contains("<a href=\"/profile/" + account.getAccountId() + "\"")) {
            return;
        }
        String csrfToken = ReUtil.get("data-csrf='(\\w+)'", homePage, 1);
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "csrf_token", csrfToken,
                "action", "enter",
                "ftaa", "",
                "bfaa", "",
                "handleOrEmail", account.getAccountId(),
                "password", account.getPassword(),
                "remember", "on",
                "_tta", ""
        );
        HttpPost post = new HttpPost("/enter");
        post.setEntity(entity);
        client.execute(post, HttpStatusValidator.SC_MOVED_TEMPORARILY);
    }

}
