package com.simplefanc.voj.judger.judge.remote.provider.codefores;

import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import org.apache.http.HttpHost;

public class CFInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo(RemoteOj.CF, "CF",
            new HttpHost("codeforces.com", 443, "https"));

}
