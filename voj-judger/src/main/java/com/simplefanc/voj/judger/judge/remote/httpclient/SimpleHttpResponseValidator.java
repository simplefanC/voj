package com.simplefanc.voj.judger.judge.remote.httpclient;

public interface SimpleHttpResponseValidator {

    SimpleHttpResponseValidator DUMMY_VALIDATOR = new SimpleHttpResponseValidator() {
        @Override
        public void validate(SimpleHttpResponse response) {
            // Validate nothing. Pass all the time.
        }
    };

    ///////////////////////////////////////////////////////////////

    void validate(SimpleHttpResponse response) throws Exception;

}
