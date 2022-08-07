package com.simplefanc.voj.judger.judge.remote.provider.poj;

import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import org.apache.http.HttpHost;

public class POJInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo(RemoteOj.POJ, "POJ", new HttpHost("poj.org"));

    static {
        INFO._64IntIoFormat = "%I64d & %I64u";
    }

}
