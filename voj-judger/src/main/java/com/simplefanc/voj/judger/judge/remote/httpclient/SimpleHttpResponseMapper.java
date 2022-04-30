package com.simplefanc.voj.judger.judge.remote.httpclient;

public interface SimpleHttpResponseMapper<T> extends Mapper<SimpleHttpResponse, T> {

    T map(SimpleHttpResponse response) throws Exception;

}
