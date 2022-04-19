package com.simplefanc.voj.judger.judge.remote.account;

import com.simplefanc.voj.common.constants.RemoteOj;
import lombok.Data;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

@Data
public class RemoteAccount {

    public final String accountId;
    public final String password;
    private final RemoteOj remoteOj;
    private final HttpContext context;

    public RemoteAccount(RemoteOj remoteOj, String accountId, String password) {
        this.remoteOj = remoteOj;
        this.accountId = accountId;
        this.password = password;
        this.context = getNewContext();
    }

    private HttpContext getNewContext() {
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        return context;
    }
}
