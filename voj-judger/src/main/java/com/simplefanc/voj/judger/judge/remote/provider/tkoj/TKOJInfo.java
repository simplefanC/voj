package com.simplefanc.voj.judger.judge.remote.provider.tkoj;

import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.judger.judge.remote.RemoteOjInfo;
import org.apache.http.HttpHost;

public class TKOJInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo(RemoteOj.TKOJ, "TKOJ", new HttpHost("tk.hustoj.com"));

    static {
        INFO._64IntIoFormat = "%I64d & %I64u";
    }

}
