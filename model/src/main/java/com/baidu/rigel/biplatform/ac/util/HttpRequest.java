/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.ac.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * httpclient 4.3 的post和get实现
 * 
 * @author xiaoming.chen
 *
 */
public class HttpRequest {

    /**
     * NO_CHECK_COOKIES
     */
    public static final String NO_CHECK_COOKIES = "NO_CHECK_COOKIES";

    /**
     * COOKIE_PARAM_NAME cookie参数的名称，参数如果是这个名称，自动放到请求的头信息中
     */
    public static final String COOKIE_PARAM_NAME = "Cookie";

    /**
     * socket timeout
     */
    public static final String SOCKET_TIME_OUT = "timeOut";

    /**
     * connTimeOut;
     */
    public static final String CONNECTION_TIME_OUT = "connTimeOut";

    /**
     * LOGGER
     */
    private static Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);

    /**
     * 获取一个默认的HttpClient，默认的是指了默认返回结果的head为application/json
     * 
     * @return 默认的HttpClient
     */
    private static HttpClient getDefaultHttpClient(Map<String, String> params) {
        return ClientInstance.getClientInstance(params);
    }

    /**
     * 获取一个默认的HttpClient，默认的是指了默认返回结果的head为application/json
     * 
     * @return 默认的HttpClient
     */
    private static HttpClient getHttpClient(Map<String, String> params) {
        return ClientInstance.getClientInstanceWithoutRoutee(params);
    }

    /**
     * 将url中的占位符用传过来的参数进行替换 需要预先解析好占位符信息，把参数封装好。推荐在传到工具类前处理好占位符。
     * 
     * @param url url
     * @param params 参数
     * @return 替换完成的url
     */
    private static String processPlaceHolder(String url, Map<String, String> params) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url is blank.");
        }
        List<String> placeHolders = PlaceHolderUtils.getPlaceHolders(url);
        if (CollectionUtils.isNotEmpty(placeHolders)) {
            String newUrl = url;
            for (String placeHolder : placeHolders) {
                String key =
                        params.containsKey(placeHolder) ? placeHolder : PlaceHolderUtils
                                .getKeyFromPlaceHolder(placeHolder);
                if (params.containsKey(key)) {
                    newUrl = PlaceHolderUtils.replacePlaceHolderWithValue(newUrl, placeHolder, params.get(key));
                }
            }
            return newUrl;
        } else {
            return url;
        }
    }

    /**
     * 向指定URL发送GET方法的请求
     * 
     * @param client httpclient对象
     * @param url 发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    private static String sendGet(HttpClient client, String url, Map<String, String> params) {
        if (client == null || StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("client is null");
        }
        if (params == null) {
            params = new HashMap<String, String>(1);
        }

        String newUrl = processPlaceHolder(url, params);
        String cookie = params.remove(COOKIE_PARAM_NAME);

        List<String> paramList = checkUrlAndWrapParam(newUrl, params, true);
        String urlNameString = "";
        if (newUrl.contains("?")) {
            paramList.add(0, newUrl);
            urlNameString = StringUtils.join(paramList, "&");
        } else {
            urlNameString = newUrl + "?" + StringUtils.join(paramList, "&");
        }

        String prefix = "", suffix = "";
        String[] addresses = new String[] { urlNameString };
        if (urlNameString.contains("[") && urlNameString.contains("]")) {
            addresses = urlNameString.substring(urlNameString.indexOf("[") + 1, urlNameString.indexOf("]")).split(" ");
            prefix = urlNameString.substring(0, urlNameString.indexOf("["));
            suffix = urlNameString.substring(urlNameString.indexOf("]") + 1);
        }
        Exception ex = null;
        for (String address : addresses) {
            String requestUrl = prefix + address + suffix;
            try {
                HttpUriRequest request = RequestBuilder.get().setUri(requestUrl).build();

                if (StringUtils.isNotBlank(cookie)) {
                    // 需要将cookie添加进去
                    request.addHeader(new BasicHeader(COOKIE_PARAM_NAME, cookie));
                }
                HttpResponse response = client.execute(request);
                String content = processHttpResponse(client, response, params, true);
                return content;
            } catch (Exception e) {
                ex = e;
                LOGGER.warn("send get error " + requestUrl + ",retry next one", e);
            }
        }
        throw new RuntimeException(ex);
    }

    /**
     * 向指定URL发送GET方法的请求（采用默认的HttpClient）
     * 
     * @param url 发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, Map<String, String> params) {
        return sendGet(getDefaultHttpClient(Collections.unmodifiableMap(params)), url, params);
    }

    /**
     * 向指定URL发送POST方法的请求
     * 
     * @param client httpclient对象
     * @param url 发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    private static String sendPost(HttpClient client, String url, Map<String, String> params,
            Map<String, String> headerParams) {
        if (client == null) {
            throw new IllegalArgumentException("client is null");
        }

        if (params == null) {
            params = new HashMap<String, String>(1);
        }
        String requestUrl = processPlaceHolder(url, params);

        String cookie = params.remove(COOKIE_PARAM_NAME);
        if (requestUrl.contains("\\?")) {
            String[] urls = requestUrl.split("\\?");
            requestUrl = urls[0];
            String[] urlParams = urls[1].split("&");
            for (String param : urlParams) {
                String[] paramSplit = param.split("=");
                if (StringUtils.isNotEmpty(paramSplit[1]) && paramSplit[1].getBytes().length > 1024 * 1024) {
                    params.put(paramSplit[0], "");
                } else {
                    params.put(paramSplit[0], paramSplit[1]);
                }
            }
        }

        List<NameValuePair> nameValues = new ArrayList<NameValuePair>();
        params.forEach((k, v) -> {
            NameValuePair nameValuePair = null;
            if (!MiniCubeConnection.QUESTIONMODEL_PARAM_KEY.endsWith(k) && StringUtils.isNotEmpty(v)) {
                String tmp = null;
                try {
                    tmp = URLEncoder.encode(v == null ? "" : v, "utf-8");
                } catch (Exception e) {
                    throw new RuntimeException("不支持utf字符编码");
                }
                if (tmp.getBytes().length > 1024 * 1024) {
                    nameValuePair = new BasicNameValuePair(k, "");
                } else {
                    nameValuePair = new BasicNameValuePair(k, v);
                }
            } else {
                nameValuePair = new BasicNameValuePair(k, v);
            }
            nameValues.add(nameValuePair);
        });

        String prefix = "", suffix = "";
        String[] addresses = new String[] { requestUrl };
        if (requestUrl.contains("[") && requestUrl.contains("]")) {
            addresses = requestUrl.substring(requestUrl.indexOf("[") + 1, requestUrl.indexOf("]")).split(" ");
            prefix = requestUrl.substring(0, requestUrl.indexOf("["));
            suffix = requestUrl.substring(requestUrl.indexOf("]") + 1);
        }
        LOGGER.info("start to send post:" + requestUrl);
        for (String address : addresses) {
            String postUrl = prefix + address + suffix;
            LOGGER.info("post url is : " + postUrl);
            try {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValues, "utf-8");
                HttpUriRequest request = RequestBuilder.post().setUri(postUrl).setEntity(entity).build();
                // 添加必要的Header属性
                request.addHeader(new BasicHeader("Accept", "*/*"));
                request.addHeader(new BasicHeader("Cache-Control", "no-cache"));
                if (StringUtils.isNotBlank(cookie)) {
                    // 需要将cookie添加进去
                    request.addHeader(new BasicHeader(COOKIE_PARAM_NAME, cookie));
                }
                if (headerParams != null && !headerParams.isEmpty()) {
                    for (String key : headerParams.keySet()) {
                        String value = headerParams.get(key);
                        request.addHeader(new BasicHeader(key, value));
                    }
                }
                LOGGER.info("[INFO] --- --- execute query with client {}", client);
                HttpResponse response = client.execute(request);
                LOGGER.info("[INFO] response --- --- " + response);
                String content = processHttpResponse(client, response, params, false);
                return content;
            } catch (Exception e) {
                LOGGER.warn("send post error " + requestUrl + ",retry next one", e);
            }
        }
        throw new RuntimeException("send post failed[" + requestUrl + "]. params :" + nameValues);

    }

    /**
     * 向指定URL发送POST方法的请求
     * 
     * @param url 发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendPost(String url, Map<String, String> params) {
        return sendPost(getDefaultHttpClient(Collections.unmodifiableMap(params)), url, params, null);
    }

    /**
     * 向指定URL发送POST方法的请求，支持header
     * 
     * @param url 发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param handerParams header请求参数。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendPost(String url, Map<String, String> params, Map<String, String> handerParams) {
        return sendPost(getDefaultHttpClient(Collections.unmodifiableMap(params)), url, params, handerParams);
    }

    /**
     * 向指定URL发送POST方法的请求
     * 
     * @param url 发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendPostWithoutRoutee(String url, Map<String, String> params) {
        return sendPost(getHttpClient(Collections.unmodifiableMap(params)), url, params, null);
    }

    /**
     * 处理HttpClient返回的结果
     * 
     * @param client HttpClient实例，为了30X状态码调用
     * @param response 返回结果
     * @param params 调用参数
     * @param isGet 是否是get方式
     * @return 返回结果
     * @throws ParseException 结果转换异常
     * @throws IOException
     */
    private static String processHttpResponse(HttpClient client, HttpResponse response, Map<String, String> params,
            boolean isGet) throws ParseException, IOException {
        try {
            StatusLine statusLine = response.getStatusLine();
            // 301 ，302 重定向支持
            if (statusLine.getStatusCode() == 301 || statusLine.getStatusCode() == 302) {
                Header header = response.getFirstHeader(HttpHeaders.LOCATION);
                LOGGER.info("get status code:" + statusLine.getStatusCode() + " redirect:" + header.getValue());
                if (isGet) {
                    return sendGet(client, header.getValue(), params);
                } else {
                    return sendPost(client, header.getValue(), params, null);
                }
            }
            if (statusLine.getStatusCode() != 200) {
                throw new IllegalStateException("Server internal error[" + statusLine.getStatusCode() + "]");
            }

            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            throw e;
        } finally {
            HttpClientUtils.closeQuietly(response);
        }

    }

    /**
     * 校验URL，并且将参数抽取整合成 key=value&key=value形式
     * 
     * @param url 访问的url
     * @param params 请求参数
     * @return 返回参数拼成字符串
     */
    private static List<String> checkUrlAndWrapParam(String url, Map<String, String> params, boolean uriEncoder) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("can not send get by null url");
        }
        List<String> paramList = new ArrayList<String>();
        if (MapUtils.isNotEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (StringUtils.isBlank(entry.getKey()) || StringUtils.isBlank(entry.getValue())) {
                    continue;
                } else {
                    String value = entry.getValue();
                    if (uriEncoder) {
                        try {
                            value = URLEncoder.encode(value, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            LOGGER.warn("encode value:" + value + "error");
                            e.printStackTrace();
                        }
                    }

                    paramList.add(entry.getKey() + "=" + value);
                }
            }

        }
        return paramList;
    }

    private static final class ClientInstance {

        private static HttpClient INSTANCE;

        private static final Object LOCK_OBJ = new Object();

        private static final CookieSpecProvider cookieSpecProvider = new CookieSpecProvider() {

            @Override
            public CookieSpec create(HttpContext context) {
                return new BrowserCompatSpec() {

                    @Override
                    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
                        // no check cookie
                    }
                };
            }
        };

        public static HttpClient getClientInstance(Map<String, String> params) {
            if (INSTANCE == null) {
                synchronized (LOCK_OBJ) {
                    if (INSTANCE == null) {
                        Lookup<CookieSpecProvider> cookieSpecRegistry =
                                RegistryBuilder.<CookieSpecProvider> create()
                                        .register(NO_CHECK_COOKIES, cookieSpecProvider).build();
                        String socketTimeout = "1800000";
                        String connTimeout = "1800000";
                        // if (params != null) {
                        // if (params.containsKey(SOCKET_TIME_OUT)) {
                        // socketTimeout = params.get(SOCKET_TIME_OUT);
                        // }
                        // if (params.containsKey(CONNECTION_TIME_OUT)) {
                        // socketTimeout = params.get(CONNECTION_TIME_OUT);
                        // }
                        // }
                        // 设置默认的cookie的安全策略为不校验
                        RequestConfig requestConfigBuilder =
                                RequestConfig.custom().setCookieSpec(NO_CHECK_COOKIES)
                                        .setSocketTimeout(Integer.valueOf(socketTimeout)) // ms ???
                                        .setConnectTimeout(Integer.valueOf(connTimeout)) // ms???
                                        .build();
                        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
                        connectionManager.setDefaultMaxPerRoute(100);
                        connectionManager.setMaxTotal(100);
                        List<HttpRoute> routee = getRoutee();
                        for (HttpRoute route : routee) {
                            connectionManager.setMaxPerRoute(route, 100);
                        }
                        INSTANCE =
                                HttpClients.custom().setDefaultCookieSpecRegistry(cookieSpecRegistry)
                                        .setDefaultRequestConfig(requestConfigBuilder)
                                        .setConnectionManager(connectionManager).build();
                    }
                }
            }

            return INSTANCE;
        }

        public static HttpClient getClientInstanceWithoutRoutee(Map<String, String> params) {
            if (INSTANCE == null) {
                synchronized (LOCK_OBJ) {
                    if (INSTANCE == null) {
                        Lookup<CookieSpecProvider> cookieSpecRegistry =
                                RegistryBuilder.<CookieSpecProvider> create()
                                        .register(NO_CHECK_COOKIES, cookieSpecProvider).build();
                        String socketTimeout = "1800000";
                        String connTimeout = "1800000";
                        // 设置默认的cookie的安全策略为不校验
                        RequestConfig requestConfigBuilder =
                                RequestConfig.custom().setCookieSpec(NO_CHECK_COOKIES)
                                        .setSocketTimeout(Integer.valueOf(socketTimeout)) // ms ???
                                        .setConnectTimeout(Integer.valueOf(connTimeout)) // ms???
                                        .build();
                        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
                        connectionManager.setDefaultMaxPerRoute(100);
                        connectionManager.setMaxTotal(100);
                        INSTANCE =
                                HttpClients.custom().setDefaultCookieSpecRegistry(cookieSpecRegistry)
                                        .setDefaultRequestConfig(requestConfigBuilder)
                                        .setConnectionManager(connectionManager).build();
                    }
                }
            }
            return INSTANCE;
        }

        private static List<HttpRoute> getRoutee() {
            String addresses = null;
            try {
                addresses = ConfigInfoUtils.getServerAddress();
            } catch (Exception e) {
                LOGGER.error("");
            }
            List<HttpRoute> routee = Lists.newArrayList();
            if (addresses == null) {
                return routee;
            }
            String[] addressArray = null;
            if (addresses.indexOf("[") < 0 || addresses.indexOf("]") < 0) {
                addressArray = new String[1];
                addressArray[0] = StringUtils.replace(addresses.split("http:")[1], "/", "");
            } else {
                String addressConf = addresses.substring(addresses.indexOf("[") + 1, addresses.lastIndexOf("]"));
                addressArray = addressConf.split(" ");
            }
            try {
                for (String addr : addressArray) {
                    int port = Integer.valueOf(addr.split(":")[1]);
                    String host = addr.split(":")[0];
                    HttpHost httpHost = new HttpHost(InetAddress.getByName(host), port);
                    HttpRoute route = new HttpRoute(httpHost);
                    routee.add(route);
                    LOGGER.info("generate route : {}", route);
                }
            } catch (UnknownHostException e) {
                LOGGER.error(e.getMessage(), e);
            }
            return routee;
        }

    }

    public static String sendPost(String url, String bodyStr, String acceptHeader,String contentType) {
        try {
            long current = System.currentTimeMillis();
            Map<String, String> params = Maps.newConcurrentMap();
            params.put(SOCKET_TIME_OUT, "1800000");
            params.put(CONNECTION_TIME_OUT, "1800000");
            HttpClient client = getDefaultHttpClient(params);
            HttpUriRequest request = RequestBuilder.post().setUri(url).setEntity(new StringEntity(bodyStr)).build();
            request.addHeader(new BasicHeader("Accept", acceptHeader));
            request.addHeader(new BasicHeader("Content-Type", contentType));
            request.addHeader(new BasicHeader("Cache-Control", "no-cache"));
            LOGGER.info("[INFO] --- --- execute query with client {}, bodyStr: {}, url :{}", client, bodyStr, url);
            HttpResponse response = client.execute(request);
            String content = processHttpResponse(client, response, Maps.newHashMap(), false);
            StringBuilder sb = new StringBuilder();
            sb.append("end send post :").append(url).append(" bodyStr:").append(bodyStr).append(" cost:")
                    .append(System.currentTimeMillis() - current);
            LOGGER.info(sb.toString());
            return content;
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
