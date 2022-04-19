package com.simplefanc.voj.judger.judge.remote.provider.eoj;


import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.judger.judge.remote.RemoteOjInfo;
import org.apache.http.HttpHost;

/**
 * @author chenfan
 * @date 2022/1/29 20:40
 **/
public class EojInfo {
    public static final RemoteOjInfo INFO = new RemoteOjInfo(
            RemoteOj.EOJ,
            "EOJ",
            new HttpHost("acm.ecnu.edu.cn", 443, "https")
    );
}
