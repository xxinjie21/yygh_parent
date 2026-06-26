package com.yygh.order.utils;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * HTTP/HTTPS 请求客户端工具类
 *
 * 功能说明：
 * 1. 支持普通HTTP请求（GET、POST、PUT）
 * 2. 支持HTTPS加密请求
 * 3. 支持微信支付SSL证书双向认证（退款场景必需）
 *
 * 使用场景：
 * - 调用微信支付统一下单接口
 * - 查询微信支付订单状态
 * - 微信退款（需要SSL证书）
 * - 调用第三方API接口
 *
 * @author XXJ
 */
public class HttpClient {

    // ==================== 成员变量 ====================

    /** 请求目标URL地址 */
    private String url;

    /** 表单参数（key-value形式） */
    private Map<String, String> param;

    /** HTTP响应状态码（如200、404、500） */
    private int statusCode;

    /** 响应内容（字符串形式） */
    private String content;

    /** XML格式参数（微信支付使用XML） */
    private String xmlParam;

    /** 是否使用HTTPS加密请求 */
    private boolean isHttps;

    /**
     * 是否使用客户端证书
     * - false：普通HTTPS，只验证服务器证书
     * - true：双向SSL认证，微信退款必需
     */
    private boolean isCert = false;

    /**
     * 证书密码
     * 微信商户号（mch_id）作为证书密码
     */
    private String certPassword;

    // ==================== Getter/Setter 方法 ====================

    public boolean isHttps() {
        return isHttps;
    }

    /**
     * 设置是否使用HTTPS
     * @param isHttps true-使用HTTPS加密，false-普通HTTP
     */
    public void setHttps(boolean isHttps) {
        this.isHttps = isHttps;
    }

    public boolean isCert() {
        return isCert;
    }

    /**
     * 设置是否使用客户端证书
     * 微信退款接口必须设置为true
     * @param cert true-使用商户证书，false-不使用证书
     */
    public void setCert(boolean cert) {
        isCert = cert;
    }

    public String getXmlParam() {
        return xmlParam;
    }

    /**
     * 设置XML格式请求参数
     * 微信支付API要求使用XML格式传输数据
     * @param xmlParam XML字符串
     */
    public void setXmlParam(String xmlParam) {
        this.xmlParam = xmlParam;
    }

    /**
     * 构造方法-带参数
     * @param url  请求URL
     * @param param 表单参数
     */
    public HttpClient(String url, Map<String, String> param) {
        this.url = url;
        this.param = param;
    }

    /**
     * 构造方法-仅URL
     * @param url 请求URL
     */
    public HttpClient(String url) {
        this.url = url;
    }

    public String getCertPassword() {
        return certPassword;
    }

    /**
     * 设置证书密码
     * 微信商户号作为证书密码
     * @param certPassword 商户号(mch_id)
     */
    public void setCertPassword(String certPassword) {
        this.certPassword = certPassword;
    }

    /**
     * 设置表单参数
     * @param map 参数Map
     */
    public void setParameter(Map<String, String> map) {
        param = map;
    }

    /**
     * 添加单个参数
     * @param key   参数名
     * @param value 参数值
     */
    public void addParameter(String key, String value) {
        if (param == null)
            param = new HashMap<String, String>();
        param.put(key, value);
    }

    // ==================== HTTP请求方法 ====================

    /**
     * 发送POST请求
     *
     * 使用场景：
     * - 微信支付统一下单
     * - 微信退款申请
     * - 提交表单数据
     *
     * 调用流程：
     * 1. 创建HttpPost对象
     * 2. 设置请求参数（表单或XML）
     * 3. 执行请求
     */
    public void post() throws ClientProtocolException, IOException {
        // 1. 创建POST请求对象
        HttpPost http = new HttpPost(url);

        // 2. 设置请求体参数
        setEntity(http);

        // 3. 执行请求
        execute(http);
    }

    /**
     * 发送PUT请求
     * 用于更新资源
     */
    public void put() throws ClientProtocolException, IOException {
        HttpPut http = new HttpPut(url);
        setEntity(http);
        execute(http);
    }

    /**
     * 发送GET请求
     *
     * 将参数拼接在URL后面：
     * http://api.example.com?key1=value1&key2=value2
     */
    public void get() throws ClientProtocolException, IOException {
        if (param != null) {
            // 使用StringBuilder拼接URL参数
            StringBuilder url = new StringBuilder(this.url);
            boolean isFirst = true;  // 标记是否是第一个参数

            // 遍历所有参数
            for (String key : param.keySet()) {
                if (isFirst) {
                    // 第一个参数前面加?
                    url.append("?");
                    isFirst = false;
                } else {
                    // 后续参数前面加&
                    url.append("&");
                }
                // 拼接 key=value
                url.append(key).append("=").append(param.get(key));
            }
            // 更新URL
            this.url = url.toString();
        }

        // 创建GET请求对象并执行
        HttpGet http = new HttpGet(url);
        execute(http);
    }

    // ==================== 参数设置方法 ====================

    /**
     * 设置HTTP请求体参数
     *
     * 支持两种格式：
     * 1. form-data格式：param != null
     * 2. XML格式：xmlParam != null（微信支付使用）
     *
     * @param http HTTP请求对象（POST/PUT）
     */
    private void setEntity(HttpEntityEnclosingRequestBase http) {
        // 方式一：设置表单参数（application/x-www-form-urlencoded）
        if (param != null) {
            // 将Map转换为NameValuePair列表
            List<NameValuePair> nvps = new LinkedList<NameValuePair>();
            for (String key : param.keySet()) {
                nvps.add(new BasicNameValuePair(key, param.get(key)));
            }
            // 设置表单实体，使用UTF-8编码
            http.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        }

        // 方式二：设置XML参数（微信支付使用）
        // XML格式用于微信支付API的数据传输
        if (xmlParam != null) {
            // 设置字符串实体，内容类型为application/xml
            http.setEntity(new StringEntity(xmlParam, Consts.UTF_8));
        }
    }

    // ==================== 核心执行方法 ====================

    /**
     * 执行HTTP/HTTPS请求
     *
     * 核心逻辑：
     * 1. 根据请求类型创建不同的HTTP客户端
     *    - 普通HTTP：HttpClients.createDefault()
     *    - HTTPS（不验证证书）：SSLContext + TrustStrategy
     *    - HTTPS（双向认证）：SSLContext + 商户证书
     * 2. 发送请求获取响应
     * 3. 解析响应状态码和内容
     * 4. 关闭连接释放资源
     *
     * @param http HTTP请求对象（GET/POST/PUT）
     */
    private void execute(HttpUriRequest http) throws ClientProtocolException, IOException {
        CloseableHttpClient httpClient = null;  // HTTP客户端引用

        try {
            // ==================== 创建HTTP客户端 ====================

            if (isHttps) {
                // ---------- HTTPS请求处理 ----------

                if (isCert) {
                    // ++++++++++++++++++++++++++++++++++++++++++++++
                    // 场景：使用SSL证书（微信退款必需）
                    // ++++++++++++++++++++++++++++++++++++++++++++++
                    //
                    // 为什么退款需要证书？
                    // - 退款是敏感操作，涉及资金安全
                    // - 微信要求验证商户身份（双向SSL认证）
                    // - 证书就是商户的"身份证明"
                    //
                    // 证书格式：PKCS#12（.p12或.pfx文件）
                    // 证书密码：商户号（mch_id）
                    //

                    // 第1步：加载证书文件
                    // ConstantPropertiesUtils.CERT = "classpath:cert/apiclient_cert.p12"
                    FileInputStream inputStream = new FileInputStream(
                        new File(ConstantPropertiesUtils.CERT));

                    // 第2步：创建KeyStore实例
                    // KeyStore用于存储密钥和证书
                    // "PKCS12"是微信证书的格式
                    KeyStore keystore = KeyStore.getInstance("PKCS12");

                    // 第3步：加载证书到KeyStore
                    // 密码为商户号(mch_id)
                    char[] partnerId2charArray = certPassword.toCharArray();
                    keystore.load(inputStream, partnerId2charArray);

                    // 第4步：创建SSLContext
                    // SSLContext是SSL/TLS协议的上下文，管理SSL连接
                    // loadKeyMaterial()方法绑定商户证书
                    SSLContext sslContext = SSLContexts.custom()
                        .loadKeyMaterial(keystore, partnerId2charArray)  // 加载商户证书
                        .build();

                    // 第5步：创建SSL连接工厂
                    // SSLConnectionSocketFactory用于创建SSL sockets
                    // - TLSv1：只支持TLS 1.0协议
                    // - BROWSER_COMPATIBLE_HOSTNAME_VERIFIER：兼容浏览器的主机名验证
                    SSLConnectionSocketFactory sslsf =
                            new SSLConnectionSocketFactory(
                                    sslContext,
                                    new String[] { "TLSv1" },  // 协议版本
                                    null,                      // 密码套件
                                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

                    // 第6步：创建带证书的HTTP客户端
                    httpClient = HttpClients.custom()
                        .setSSLSocketFactory(sslsf)
                        .build();

                } else {
                    // ++++++++++++++++++++++++++++++++++++++++++++++
                    // 场景：普通HTTPS请求（不验证证书）
                    // ++++++++++++++++++++++++++++++++++++++++++++++
                    //
                    // 使用场景：调用微信支付统一下单接口
                    // 特点：信任所有证书，不验证服务器身份
                    // 原因：微信服务器证书可能不被JDK默认信任
                    //

                    // 第1步：创建SSLContextBuilder
                    SSLContextBuilder sslContextBuilder = new SSLContextBuilder();

                    // 第2步：加载信任材料
                    // loadTrustMaterial(null, TrustStrategy)
                    // - 第一个参数null：使用系统默认的信任管理器
                    // - 第二个参数：自定义信任策略
                    //
                    // TrustStrategy接口的isTrusted()方法：
                    // - 返回true：信任所有证书（不安全，仅用于开发）
                    // - 返回false：使用系统默认验证
                    SSLContext sslContext = sslContextBuilder
                            .loadTrustMaterial(null, new TrustStrategy() {
                                /**
                                 * 信任所有证书
                                 *
                                 * @param chain    证书链
                                 * @param authType 认证类型
                                 * @return true-信任该证书，false-拒绝
                                 */
                                public boolean isTrusted(X509Certificate[] chain,
                                                         String authType)
                                        throws CertificateException {
                                    return true;  // 信任所有证书
                                }
                            }).build();

                    // 第3步：创建SSL连接工厂（不验证主机名）
                    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                            sslContext);

                    // 第4步：创建HTTP客户端
                    httpClient = HttpClients.custom()
                        .setSSLSocketFactory(sslsf)
                        .build();
                }

            } else {
                // ---------- 普通HTTP请求 ----------
                //
                // 使用HttpClients.createDefault()创建默认客户端
                // 支持基本的HTTP请求
                httpClient = HttpClients.createDefault();
            }

            // ==================== 发送请求 ====================

            // 执行HTTP请求，获取响应
            CloseableHttpResponse response = httpClient.execute(http);

            try {
                // ==================== 解析响应 ====================

                if (response != null) {
                    // 获取HTTP状态码
                    // 200：成功
                    // 404：资源不存在
                    // 500：服务器内部错误
                    if (response.getStatusLine() != null)
                        statusCode = response.getStatusLine().getStatusCode();

                    // 获取响应实体（HTTP响应的body部分）
                    HttpEntity entity = response.getEntity();

                    // 将响应实体转换为字符串
                    // 使用UTF-8编码解析
                    // EntityUtils.toString()会消费实体内容
                    content = EntityUtils.toString(entity, Consts.UTF_8);
                }

            } finally {
                // ==================== 关闭响应流 ====================
                //
                // 重要：必须关闭响应流，释放连接资源
                // 使用finally确保无论是否异常都会执行
                response.close();
            }

        } catch (Exception e) {
            // 异常处理：打印堆栈信息
            // 实际项目中应该使用日志框架（log.error）
            e.printStackTrace();

        } finally {
            // ==================== 关闭HTTP客户端 ====================
            //
            // 重要：必须关闭HTTP客户端，释放连接池资源
            // 使用finally确保资源一定被释放
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }

    // ==================== 响应获取方法 ====================

    /**
     * 获取HTTP响应状态码
     * @return HTTP状态码（如200、404、500）
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * 获取响应内容
     * @return 响应体字符串
     */
    public String getContent() throws ParseException, IOException {
        return content;
    }
}

/**
 * 使用示例：
 *
 * // 1. 微信支付统一下单（普通HTTPS）
 * HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
 * client.setXmlParam(xml);
 * client.setHttps(true);
 * client.post();
 * String result = client.getContent();
 *
 * // 2. 微信退款（需要证书）
 * HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
 * client.setXmlParam(xml);
 * client.setHttps(true);
 * client.setCert(true);
 * client.setCertPassword("商户号");
 * client.post();
 * String result = client.getContent();
 *
 * // 3. GET请求
 * HttpClient client = new HttpClient("http://api.example.com");
 * client.addParameter("key", "value");
 * client.get();
 * String result = client.getContent();
 */
