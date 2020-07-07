package com.starix.gdou.utils;

import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对httpclient进行一层封装
 *
 * @author shiwenjie
 * @created 2020/6/28 5:41 下午
 **/
public class HttpClientUtil {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/79.0.3945.130 Safari/537.36";

    private CookieStore cookieStore = new BasicCookieStore();

    // 为了隔离每个请求的httpClient实例，使彼此之间互不干扰，这里不采用static
    private HttpClient httpClient = createHttpClient();


    public CloseableHttpClient createHttpClient() {
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (x509Certificates, s) -> true).build();
            CloseableHttpClient client = HttpClients.custom()
                    // 忽略ssl验证
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    // 自动跟踪重定向(支持POST)
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    // cookie管理
                    .setDefaultCookieStore(cookieStore)
                    .build();
            return client;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String doGet(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", USER_AGENT);
        return executeRequest(httpGet);
    }

    public String doGet(String url, Map<String, String> params) throws Exception {
        url = buildUrlWithParams(url, params);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", USER_AGENT);
        return executeRequest(httpGet);
    }

    public String doGet(String url, Map<String, String> params, Map<String, String> headerParams) throws Exception {
        url = buildUrlWithParams(url, params);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", USER_AGENT);
        for (Map.Entry<String, String> headerParam : headerParams.entrySet()) {
            httpGet.setHeader(headerParam.getKey(), headerParam.getValue());
        }
        return executeRequest(httpGet);
    }

    public String doPost(String url, Map<String, String> formData) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", USER_AGENT);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        List<NameValuePair> paramList = new ArrayList<>();
        for (Map.Entry<String, String> param : formData.entrySet()) {
            paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        // 表单类型post参数
        httpPost.setEntity(new UrlEncodedFormEntity(paramList, Consts.UTF_8));
        return executeRequest(httpPost);
    }

    public String doPost(String url, String json) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", USER_AGENT);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        // json类型post参数
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        return executeRequest(httpPost);
    }

    public String doPost(String url, Map<String, String> formData, Map<String, String> headerParams) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", USER_AGENT);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        for (Map.Entry<String, String> headerParam : headerParams.entrySet()) {
            httpPost.setHeader(headerParam.getKey(), headerParam.getValue());
        }
        List<NameValuePair> paramList = new ArrayList<>();
        for (Map.Entry<String, String> param : formData.entrySet()) {
            paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        // 表单类型post参数
        httpPost.setEntity(new UrlEncodedFormEntity(paramList, Consts.UTF_8));
        return executeRequest(httpPost);
    }

    public String doPost(String url, String json, Map<String, String> headerParams) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", USER_AGENT);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        for (Map.Entry<String, String> headerParam : headerParams.entrySet()) {
            httpPost.setHeader(headerParam.getKey(), headerParam.getValue());
        }
        // json类型post参数
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        return executeRequest(httpPost);
    }

    public String getCookie(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        List<Cookie> cookies = cookieStore.getCookies();
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void addCookie(String name, String value, Map<String, String> settings) {
        if (name == null || name.isEmpty()) {
            return;
        }
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setPath(settings.get("path"));
        cookie.setDomain(settings.get("domain"));
        cookieStore.addCookie(cookie);
    }

    private String executeRequest(HttpRequestBase httpRequest) throws IOException {
        try {
            HttpResponse response = httpClient.execute(httpRequest);
            HttpEntity httpEntity = response.getEntity();
            if (!checkResult(response)) {
                throw new RuntimeException(
                        String.format("http请求响应状态异常, 响应状态吗：%s, 响应报文：%s",
                        response.getStatusLine().getStatusCode(),
                        EntityUtils.toString(httpEntity, Consts.UTF_8)));
            }
            return EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        } catch (IOException e) {
            throw e;
        } finally {
            httpRequest.releaseConnection();
        }
    }

    private boolean checkResult(HttpResponse response){
        return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                || response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY)
                && response.getEntity() != null;
    }

    public static String buildUrlWithParams(String url, Map<String, String> params) throws Exception {
        if (params == null || params.isEmpty()) {
            return url;
        }
        URIBuilder uriBuilder = new URIBuilder(url);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }
        return uriBuilder.build().toString();
    }

}