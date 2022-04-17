package com.simplefanc.voj.remoteJudge.provider.jsk;

import com.simplefanc.voj.pojo.RemoteOj;
import com.simplefanc.voj.remoteJudge.RemoteOjInfo;
import org.apache.http.HttpHost;

public class JSKInfo {
    public static final RemoteOjInfo INFO = new RemoteOjInfo(
            RemoteOj.JSK,
            "JSK",
            new HttpHost("nanti.jisuanke.com", 443, "https")
    );

    public static final RemoteOjInfo LOGIN = new RemoteOjInfo(
            RemoteOj.JSK,
            "JSK",
            new HttpHost("passport.jisuanke.com", 443, "https")
    );
}
