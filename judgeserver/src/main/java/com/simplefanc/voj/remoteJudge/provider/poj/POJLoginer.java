package com.simplefanc.voj.remoteJudge.provider.poj;


import com.simplefanc.voj.remoteJudge.RemoteOjInfo;
import com.simplefanc.voj.remoteJudge.account.RemoteAccount;
import com.simplefanc.voj.remoteJudge.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.remoteJudge.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.remoteJudge.httpclient.HttpStatusValidator;
import com.simplefanc.voj.remoteJudge.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.remoteJudge.loginer.RetentiveLoginer;
import org.apache.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class POJLoginer extends RetentiveLoginer {
    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return POJInfo.INFO;
    }

    @Override
    protected void loginEnforce(RemoteAccount account) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        if (client.get("/").getBody().contains(">Log Out</a>")) {
            return;
        }
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "B1", "login",
                "password1", account.getPassword(),
                "url", "%2F",
                "user_id1", account.getAccountId());
        client.post("/login", entity, HttpStatusValidator.SC_MOVED_TEMPORARILY);
    }
}
