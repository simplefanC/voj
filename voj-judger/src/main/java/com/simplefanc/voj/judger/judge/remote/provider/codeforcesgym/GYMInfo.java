package com.simplefanc.voj.judger.judge.remote.provider.codeforcesgym;

import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import org.apache.http.HttpHost;

public class GYMInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo(RemoteOj.GYM, "GYM",
            new HttpHost("codeforces.com", 443, "https"));

}
