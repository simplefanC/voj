package com.simplefanc.voj.judger.judge.remote.httpclient;

public interface Mapper<S, T> {

    T map(S value) throws Exception;

}
