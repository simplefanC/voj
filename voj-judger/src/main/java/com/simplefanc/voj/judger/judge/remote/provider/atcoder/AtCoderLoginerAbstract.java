package com.simplefanc.voj.judger.judge.remote.provider.atcoder;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.loginer.AbstractRetentiveLoginer;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class AtCoderLoginerAbstract extends AbstractRetentiveLoginer {

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return AtCoderInfo.INFO;
    }

    @Override
    protected void loginEnforce(RemoteAccount account) {
//        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
//        final String body = client.get("/").getBody();
//        if (body.contains("Sign Out")) {
//            return;
//        }
//        String csrfToken = ReUtil.get("var csrfToken = \"([\\s\\S]*?)\"", body, 1);
//        account.setCsrfToken(csrfToken);
//        HttpEntity entity = SimpleNameValueEntityFactory.create(
//                "csrf_token", csrfToken,
//                "username", account.getAccountId(),
//                "password", account.getPassword()
//        );
////        final HttpPost post = new HttpPost("/login");
////        post.setEntity(entity);
////        client.execute(post, HttpStatusValidator.SC_MOVED_TEMPORARILY);
//        client.post("/login", entity, HttpStatusValidator.SC_MOVED_TEMPORARILY);
        HttpRequest.getCookieManager().getCookieStore().removeAll();
        final String body = HttpUtil.createGet("https://atcoder.jp/login").execute().body();
        String csrfToken = ReUtil.get("var csrfToken = \"([\\s\\S]*?)\"", body, 1);
        final HttpResponse response = HttpUtil.createPost("https://atcoder.jp/login").form(MapUtil.builder(new HashMap<String, Object>())
                .put("username", account.getAccountId())
                .put("password", account.getPassword())
                .put("csrf_token", csrfToken).map()).execute();
        Assert.isTrue(response.getStatus() == HttpStatus.SC_MOVED_TEMPORARILY,
                String.format("expected=%s, received=%s", HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus()));
        account.setCsrfToken(csrfToken);
        account.setCookies(response.getCookies());
    }
}
