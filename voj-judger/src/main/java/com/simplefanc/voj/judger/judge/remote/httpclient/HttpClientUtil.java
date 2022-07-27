package com.simplefanc.voj.judger.judge.remote.httpclient;

import com.alibaba.nacos.common.utils.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * https://blog.csdn.net/qq_19642249/article/details/103817546
 */
@Slf4j(topic = "voj")
public class HttpClientUtil {

    private static final String _HTTP = "http";

    private static final String _HTTPS = "https";

    // 配置连接池获取超时时间
    private static final int CONNECTION_REQUEST_TIMEOUT = 1 * 1000;

    // 配置客户端连接服务器超时时间
    private static final int CONNECT_TIMEOUT = 3 * 1000;

    // 配置服务器响应超时时间
    private static final int SOCKET_TIMEOUT = 20 * 1000;

    private static final int MAX_CONN_TOTAL = 20;

    private static final int MAX_CONN_PER_ROUTE = 4;

    // 默认返回null串
    private static String EMPTY_STR = "";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

    private static SSLConnectionSocketFactory sslConnectionSocketFactory = null;

    // 连接池管理类
    private static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = null;

    // 管理Https连接的上下文类
    private static SSLContextBuilder sslContextBuilder = null;

    static {
        try {
            sslContextBuilder = SSLContexts.custom();
            sslContextBuilder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            SSLContext sslContext = sslContextBuilder.build();
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {
                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                }

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }

                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });

            // 注册两种请求形式
            Registry<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(_HTTP, new PlainConnectionSocketFactory()).register(_HTTPS, sslConnectionSocketFactory)
                    .build();
            poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registryBuilder);
            // 最大连接数
            poolingHttpClientConnectionManager.setMaxTotal(MAX_CONN_TOTAL);
            // 最大并发数
            poolingHttpClientConnectionManager.setDefaultMaxPerRoute(MAX_CONN_PER_ROUTE);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * http初始化连接配置
     */
    private static RequestConfig getDefaultRequestConfig() {
        return RequestConfig.custom()
                /*
                 * 从连接池中获取连接的超时时间，假设：连接池中已经使用的连接数等于setMaxTotal，新来的线程在等待1*1000
                 * 后超时，错误内容：org.apache.http.conn.ConnectionPoolTimeoutException: Timeout
                 * waiting for connection from pool
                 */
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                /*
                 * 这定义了通过网络与服务器建立连接的超时时间。
                 * Httpclient包中通过一个异步线程去创建与服务器的socket连接，这就是该socket连接的超时时间，
                 * 此处设置为2秒。假设：访问一个IP，192.168.10.100，这个IP不存在或者响应太慢，那么将会返回
                 * java.net.SocketTimeoutException: connect timed out
                 */
                .setConnectTimeout(CONNECT_TIMEOUT)
                /*
                 * 指的是连接上一个url，获取response的返回等待时间，假设：url程序中存在阻塞、或者response
                 * 返回的文件内容太大，在指定的时间内没有读完，则出现 java.net.SocketTimeoutException: Read timed
                 * out
                 */
                .setSocketTimeout(SOCKET_TIMEOUT).build();
    }

    /**
     * http初始化keep-Alive配置
     */
    public static ConnectionKeepAliveStrategy getKeepAliveStrategy() {
        return new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && "timeout".equalsIgnoreCase(param)) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                // 如果没有约定，则默认定义时长为20s
                return 20 * 1000;
            }
        };
    }

    /**
     * 从池中获取获取httpclient连接
     */
    public static CloseableHttpClient getHttpClient() {
        return HttpClients.custom()
                // 设置ssl工厂
                .setSSLSocketFactory(sslConnectionSocketFactory)
                // 设置连接管理方式-连接池
                .setConnectionManager(poolingHttpClientConnectionManager)
                // 设置http请求规则
                .setDefaultRequestConfig(getDefaultRequestConfig())
                // 设置keep-Alive
//                .setKeepAliveStrategy(getKeepAliveStrategy())
                .setUserAgent(USER_AGENT).build();
    }

    /**
     * post请求——JSON格式
     */
    public static String postJSON(String url, String json) {

        HttpPost httpPost = new HttpPost(url);
        // 解决中文乱码问题
        StringEntity entity = new StringEntity(json, Consts.UTF_8);
        entity.setContentType("application/json;charset=UTF-8");
        httpPost.setEntity(entity);
        return getResult(httpPost);
    }

    /**
     * post请求——form格式
     */
    public static String postForm(String url, Map<String, String> params) {

        HttpPost httpPost = new HttpPost(url);
        // 拼装参数，设置编码格式
        if (MapUtils.isNotEmpty(params)) {
            List<NameValuePair> paramList = new ArrayList<>();
            for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
                paramList.add(new BasicNameValuePair(stringStringEntry.getKey(), stringStringEntry.getValue()));
            }
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(paramList, Consts.UTF_8);
            httpPost.setEntity(urlEncodedFormEntity);
        }
        return getResult(httpPost);
    }

    /**
     * 通用版处理http请求
     */
    private static String getResult(HttpRequestBase request) {

        /**
         * 获取httpClient
         */
        CloseableHttpClient httpClient = null;
        try {
            // 获取httpClient
            httpClient = getHttpClient();
        } catch (Exception e) {
            log.error("【新版http】获取httpClient失败:请求地址:{},异常信息：", request.getURI(), e);
            throw new RuntimeException("获取httpClient失败");
        }
        /**
         * 发起http请求,并处理响应结果
         */
        String resultStr = null;
        CloseableHttpResponse httpResponse = null;
        try {
            // 发起http请求
            httpResponse = httpClient.execute(request);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            // 响应成功
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity httpResponseEntity = httpResponse.getEntity();
                resultStr = EntityUtils.toString(httpResponseEntity);
                log.info("【新版http】请求正常,请求地址:{},响应结果:{}", request.getURI(), resultStr);
                return resultStr;
            }
            // 响应失败，打印http异常信息
            StringBuffer stringBuffer = new StringBuffer();
            HeaderIterator headerIterator = httpResponse.headerIterator();
            while (headerIterator.hasNext()) {
                stringBuffer.append("\t" + headerIterator.next());
            }
            log.info("【新版http】异常信息:请求地址:{},响应状态:{},请求返回结果:{}", request.getURI(), statusCode, stringBuffer);
        } catch (Exception e) {
            log.error("【新版http】发生异常:请求地址:{},异常信息：", request.getURI(), e);
            throw new RuntimeException("http请求失败");
        } finally {
            // 关闭httpResponse
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return EMPTY_STR;
    }

}
