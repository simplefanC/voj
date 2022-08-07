package com.simplefanc.voj.judger.judge.remote.provider.hdu;

import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.judger.judge.remote.loginer.Loginer;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HDULoginer implements Loginer {
    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return HDUInfo.INFO;
    }

    @Override
    public void login(RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        if (client.get("/").getBody().contains("href=\"/userloginex.php?action=logout\"")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "username", account.getAccountId(),
                "userpass", account.getPassword()
        );
        client.post(
                "/userloginex.php?action=login&cid=0&notice=0",
                entity,
                HttpStatusValidator.SC_MOVED_TEMPORARILY);
    }
}