package com.simplefanc.voj.judger.judge.remote.provider.mxt;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class MXTLoginer extends RetentiveLoginer {

    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return MXTInfo.INFO;
    }

    private String getCaptcha(DedicatedHttpClient client) throws ClientProtocolException, IOException {
        HttpGet get = new HttpGet("/code");
        File gif = client.execute(get, new ResponseHandler<File>() {
            @Override
            public File handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                File captchaGif = File.createTempFile("mxt", ".gif");
                try (FileOutputStream fos = new FileOutputStream(captchaGif)) {
                    response.getEntity().writeTo(fos);
                    return captchaGif;
                }
            }
        });
        return MXTCaptchaRecognizer.recognize(gif);
    }

    @Override
    protected void loginEnforce(RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        // 获得SHRIOSESSIONID
        client.get("/login");
        if (client.execute(new HttpPost("/islogin"), HttpStatusValidator.SC_OK).getBody().contains("1")) {
            return;
        }
        HttpEntity entity = SimpleNameValueEntityFactory.create("username", account.getAccountId(), "password",
                account.getPassword(), "valiCode", getCaptcha(client));

        HttpPost post = new HttpPost("/login");
        post.setEntity(entity);
        client.execute(post, HttpStatusValidator.SC_MOVED_TEMPORARILY);
    }

}