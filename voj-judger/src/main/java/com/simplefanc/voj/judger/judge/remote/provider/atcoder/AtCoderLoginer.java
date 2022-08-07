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
import com.simplefanc.voj.judger.judge.remote.loginer.RetentiveLoginer;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class AtCoderLoginer extends RetentiveLoginer {

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

    public static void main(String[] args) {
//        HttpRequest.getCookieManager().getCookieStore();
        final String token = extracted();
        HttpRequest request = HttpUtil.createPost("https://atcoder.jp/login");
        HttpResponse response = request.form(MapUtil.builder(new HashMap<String, Object>())
                .put("username", "ecnuv1")
                .put("password", "123456Ecnu")
                .put("csrf_token", token).map())
                .execute();
//        final List<HttpCookie> cookies = response.getCookies();
//        System.out.println(Thread.currentThread());
//        System.out.println();
//        HttpCookie cookie = new HttpCookie();
//        BasicClientCookie
        //        final CookieStore cookieStore = (CookieStore) client.getContext().getAttribute(HttpClientContext.COOKIE_STORE);
//        cookieStore.addCookie(cookie);
    }

    private static String extracted() {
        System.out.println(Thread.currentThread());
        HttpRequest request = HttpUtil.createGet("https://atcoder.jp");
        HttpResponse response = request.execute();
        String body = response.body();
        return ReUtil.get("var csrfToken = \"([\\s\\S]*?)\"", body, 1);
    }


}
