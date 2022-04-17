package com.simplefanc.voj.remoteJudge.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HttpContext;

/**
 * 专用 Http 客户端
 */
public class DedicatedHttpClient {

    protected HttpHost host;
    protected HttpContext context;
    protected String charset;
    /**
     * CloseableHttpClient
     */
    protected HttpClient client;
    protected AnonymousHttpContextRepository contextRepository;

    protected DedicatedHttpClient() {
    }

    // /////////////////////////////////////////////////////////

    public <T> T execute(final HttpRequest request, final SimpleHttpResponseMapper<T> mapper) {
        HttpContext _context = context != null ? context : contextRepository.acquire();
        try {
            // public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
            return client.execute(host, request, new ResponseHandler<T>() {
                @Override
                public T handleResponse(HttpResponse response) {
                    try {
                        SimpleHttpResponse simpleHttpResponse = SimpleHttpResponse.build(response, charset);
                        return mapper.map(simpleHttpResponse);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, _context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (context == null && _context != null) {
                contextRepository.release(_context);
            }
        }
    }

    public SimpleHttpResponse execute(final HttpRequest request, final SimpleHttpResponseValidator... validators) {
        return execute(request, new SimpleHttpResponseMapper<SimpleHttpResponse>() {
            @Override
            public SimpleHttpResponse map(SimpleHttpResponse response) throws Exception {
                request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
                request.setHeader("Accept-Language", "en-GB,en;q=0.8");
                for (SimpleHttpResponseValidator validator : validators) {
                    if (validator != null) {
                        validator.validate(response);
                    }
                }
                return response;
            }
        });
    }

    public SimpleHttpResponse execute(final HttpRequest request) {
        return execute(request, SimpleHttpResponseValidator.DUMMY_VALIDATOR);
    }

    public <T> T execute(final HttpRequest request, final ResponseHandler<T> handler) {
        HttpContext _context = context != null ? context : contextRepository.acquire();
        try {
            return client.execute(host, request, handler, _context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (context == null && _context != null) {
                contextRepository.release(_context);
            }
        }
    }

    // /////////////////////////////////////////////////////////

    public <T> T get(String url, SimpleHttpResponseMapper<T> mapper) {
        return execute(new HttpGet(url), mapper);
    }

    public SimpleHttpResponse get(String url, SimpleHttpResponseValidator... validators) {
        return execute(new HttpGet(url), validators);
    }

    public SimpleHttpResponse get(String url) {
        return get(url, SimpleHttpResponseValidator.DUMMY_VALIDATOR);
    }

    // /////////////////////////////////////////////////////////

    public <T> T post(String url, SimpleHttpResponseMapper<T> mapper) {
        return execute(new HttpPost(url), mapper);
    }

    public SimpleHttpResponse post(String url, SimpleHttpResponseValidator... validators) {
        return execute(new HttpPost(url), validators);
    }

    public SimpleHttpResponse post(String url) {
        return post(url, SimpleHttpResponseValidator.DUMMY_VALIDATOR);
    }

    // /////////////////////////////////////////////////////////

    public <T> T post(String url, HttpEntity entity, SimpleHttpResponseMapper<T> mapper) {
        HttpPost post = new HttpPost(url);
        post.setEntity(entity);
        return execute(post, mapper);
    }

    public SimpleHttpResponse post(String url, HttpEntity entity, SimpleHttpResponseValidator... validators) {
        HttpPost post = new HttpPost(url);
        post.setEntity(entity);
        return execute(post, validators);
    }

    public SimpleHttpResponse post(String url, HttpEntity entity) {
        return post(url, entity, SimpleHttpResponseValidator.DUMMY_VALIDATOR);
    }

    // ////////////////////////////////////////////////////////

    public HttpContext getContext() {
        return context;
    }

}
