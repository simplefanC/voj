package com.simplefanc.voj.judger.judge.remote.provider.eoj;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import com.simplefanc.voj.judger.judge.remote.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.judger.judge.remote.loginer.RetentiveLoginer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@Component
public class EojLoginer extends RetentiveLoginer {

    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return EojInfo.INFO;
    }

    @Override
    protected void loginEnforce(RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        final String body = client.get("/login/").getBody();
        if (body.contains("logout")) {
            return;
        }
        String csrfmiddlewaretoken = ReUtil.getGroup1("name='csrfmiddlewaretoken' value='([\\s\\S]*?)'", body);
        String publicKey = ReUtil
                .getGroup1("name=\"public_key\" placeholder=\"\" type=\"hidden\" value=\"([\\s\\S]*?)\"", body);
        final String captcha_0 = ReUtil.getGroup1("name=\"captcha_0\" value=\"([\\s\\S]*?)\"", body);
        final String ciphertext = HttpUtil.post("127.0.0.1:9898/rsa", MapUtil.builder(new HashMap<String, Object>())
                .put("publicKey", publicKey).put("password", account.password).build());
        HttpPost post = new HttpPost("/login");
        HttpEntity entity = SimpleNameValueEntityFactory.create("csrfmiddlewaretoken", csrfmiddlewaretoken, "next",
                "/login/", "username", account.accountId, "password", ciphertext, "captcha_0", captcha_0, "captcha_1",
                getCaptcha(client, "/captcha/image/" + captcha_0 + "/"), "public_key", publicKey);
        post.setEntity(entity);
        // post.setHeader("", "");
        // client.execute(post, HttpStatusValidator.SC_MOVED_TEMPORARILY, new
        // HttpBodyValidator("success"));
        client.execute(post, HttpStatusValidator.SC_MOVED_TEMPORARILY);
    }

    private String getCaptcha(DedicatedHttpClient client, String url) throws ClientProtocolException, IOException {
        HttpGet get = new HttpGet(url);
        InputStream imgBytes = client.execute(get, new ResponseHandler<InputStream>() {
            @Override
            public InputStream handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                return response.getEntity().getContent();
            }
        });
        return HttpUtil.post("127.0.0.1:9898/ocr",
                MapUtil.builder(new HashMap<String, Object>()).put("image", imgBytes).build());
    }

}
