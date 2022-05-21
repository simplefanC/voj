package com.simplefanc.voj.judger.judge.remote.httpclient;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.protocol.HttpContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DedicatedHttpClientFactory {

    private final AnonymousHttpContextRepository contextRepository;

    public DedicatedHttpClient build(HttpHost host, HttpContext context, String charset) {
        DedicatedHttpClient client = new DedicatedHttpClient();
        client.host = host;
        client.context = context;
        client.charset = charset;
        client.client = HttpClientUtil.getHttpClient();
        client.contextRepository = contextRepository;
        return client;
    }

    public DedicatedHttpClient build(HttpHost host, String charset) {
        return build(host, null, charset);
    }

    public DedicatedHttpClient build(HttpHost host, HttpContext context) {
        return build(host, context, "UTF-8");
    }

    public DedicatedHttpClient build(HttpHost host) {
        return build(host, null, "UTF-8");
    }

    // private CloseableHttpClient getHttpClinet(
    // int socketTimeout,
    // int connectionTimeout,
    // int maxConnTotal,
    // int maxConnPerRoute,
    // String userAgent) throws NoSuchAlgorithmException, KeyStoreException,
    // KeyManagementException {
    // SSLContextBuilder contextBuilder = SSLContexts.custom();
    // contextBuilder.loadTrustMaterial(null, new TrustStrategy() {
    // @Override
    // public boolean isTrusted(X509Certificate[] chain, String authType) throws
    // CertificateException {
    // //忽略http校验
    // return true;
    // }
    // });
    // SSLConnectionSocketFactory sslConnectionSocketFactory = new
    // SSLConnectionSocketFactory(contextBuilder.build(), new String[]{"SSLv3", "TLSv1",
    // "TLSv1.2"}, null, null);
    // //注册两种请求形式
    // Registry<ConnectionSocketFactory> socketFactoryRegistry =
    // RegistryBuilder.<ConnectionSocketFactory>create()
    // .register("https", sslConnectionSocketFactory)
    // .register("http", PlainConnectionSocketFactory.getSocketFactory())
    // .build();
    //
    // PoolingHttpClientConnectionManager cm = new
    // PoolingHttpClientConnectionManager(socketFactoryRegistry);
    //
    // RequestConfig config = RequestConfig.custom()
    // // 配置服务器响应超时时间(连接上一个url，获取response的返回等待时间)
    // .setSocketTimeout(socketTimeout)
    // // 配置客户端连接服务器超时时间
    // .setConnectTimeout(connectionTimeout)
    // .build();
    //
    // return HttpClients.custom()
    // // 设置连接管理方式-连接池
    // .setConnectionManager(cm)
    // // 最大连接数
    // .setMaxConnTotal(maxConnTotal)
    // // 最大并发数
    // .setMaxConnPerRoute(maxConnPerRoute)
    // .setUserAgent(userAgent)
    // // 设置http请求规则
    // .setDefaultRequestConfig(config)
    // .build();
    // }

}
