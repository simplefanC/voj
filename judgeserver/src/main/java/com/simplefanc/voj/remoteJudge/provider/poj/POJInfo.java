package com.simplefanc.voj.remoteJudge.provider.poj;

import com.simplefanc.voj.pojo.RemoteOj;
import com.simplefanc.voj.remoteJudge.RemoteOjInfo;
import org.apache.http.HttpHost;

public class POJInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo(
            RemoteOj.POJ,
            "POJ",
            new HttpHost("poj.org")
    );

    static {
        INFO._64IntIoFormat = "%I64d & %I64u";
    }

}
