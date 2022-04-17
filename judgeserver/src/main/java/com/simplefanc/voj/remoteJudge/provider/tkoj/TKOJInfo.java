package com.simplefanc.voj.remoteJudge.provider.tkoj;

import com.simplefanc.voj.pojo.RemoteOj;
import com.simplefanc.voj.remoteJudge.RemoteOjInfo;
import org.apache.http.HttpHost;

public class TKOJInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo(
            RemoteOj.TKOJ,
            "TKOJ",
            new HttpHost("tk.hustoj.com")
    );

    static {
        INFO._64IntIoFormat = "%I64d & %I64u";
    }
}
