package com.simplefanc.voj.judger.judge.remote.httpclient;

import cn.hutool.core.lang.Assert;

public class HttpBodyValidator implements SimpleHttpResponseValidator {

    private String subString;

    private boolean negate;

    public HttpBodyValidator(String subString) {
        this(subString, false);
    }

    public HttpBodyValidator(String subString, boolean negate) {
        this.subString = subString;
        this.negate = negate;
    }

    @Override
    public void validate(SimpleHttpResponse response) throws Exception {
        try {
            Assert.isTrue(response.getBody().contains(subString) ^ negate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
