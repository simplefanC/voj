package com.simplefanc.voj.judger.judge.remote.provider.hdu;

import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import org.apache.http.HttpHost;

public class HDUInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo(
            RemoteOj.HDU,
            "HDU",
            new HttpHost("acm.hdu.edu.cn")
    );

}