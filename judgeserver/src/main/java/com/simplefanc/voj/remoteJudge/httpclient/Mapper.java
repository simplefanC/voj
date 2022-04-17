package com.simplefanc.voj.remoteJudge.httpclient;

public interface Mapper<S, T> {

    T map(S value) throws Exception;

}
