package com.yygh.common.helper;

import com.alibaba.fastjson.JSONObject;
import com.yygh.common.exception.YyghException;
import com.yygh.common.result.ResultCodeEnum;
import com.yygh.common.utils.HttpUtil;
import com.yygh.common.utils.MD5;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * HTTP请求工具辅助类，提供签名生成/校验及请求发送功能
 *
 * @author XXJ
 */
@Slf4j
public class HttpRequestHelper {

    /** 签名有效时间窗口（5分钟），超出视为过期请求，用于防重放 */
    private static final long SIGN_EXPIRE_MILLIS = 5 * 60 * 1000;

    /**
     * 请求数据获取签名
     *
     * <p><b>安全说明</b>：此处<b>严禁</b>打印参与签名的原始串。
     * 原始串中包含医院的 signKey（等同于密钥）、患者身份证号与手机号，
     * 一旦写入日志等同于凭据泄露 —— 拿到 signKey 即可伪造签名篡改医院数据。
     *
     * @param paramMap 参与签名的参数（注意：本方法会移除其中已有的 sign 键）
     * @param signKey  医院签名密钥
     * @return MD5 签名
     */
    public static String getSign(Map<String, Object> paramMap, String signKey) {
        if (paramMap.containsKey("sign")) {
            paramMap.remove("sign");
        }
        TreeMap<String, Object> sorted = new TreeMap<>(paramMap);
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, Object> param : sorted.entrySet()) {
            str.append(param.getValue()).append("|");
        }
        str.append(signKey);
        return MD5.encrypt(str.toString());
    }

    /**
     * 签名校验，并校验时间戳防重放
     *
     * @return true 表示签名一致且在有效时间窗口内
     */
    public static boolean isSignEquals(Map<String, Object> paramMap, String signKey) {
        String sign = (String) paramMap.get("sign");
        if (sign == null || sign.isEmpty()) {
            log.warn("请求缺少 sign 参数，签名校验失败");
            return false;
        }
        // 防重放：时间戳超出有效窗口的请求直接拒绝，避免请求被截获后无限次重放
        if (!isTimestampValid(paramMap.get("timestamp"))) {
            log.warn("请求时间戳非法或已过期，疑似重放攻击");
            return false;
        }
        String md5Str = getSign(paramMap, signKey);
        // 恒定时间比较，降低时序攻击风险
        return MessageDigest.isEqual(sign.getBytes(StandardCharsets.UTF_8),
                md5Str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验时间戳是否落在允许的时间窗口内
     */
    private static boolean isTimestampValid(Object timestampObj) {
        if (timestampObj == null) {
            return false;
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(String.valueOf(timestampObj));
        } catch (NumberFormatException e) {
            return false;
        }
        return Math.abs(System.currentTimeMillis() - timestamp) <= SIGN_EXPIRE_MILLIS;
    }

    /**
     * 获取时间戳
     * @return
     */
    public static long getTimestamp() {
        return new Date().getTime();
    }

    /**
     * 封装同步请求
     * @param paramMap
     * @param url
     * @return
     */
    public static JSONObject sendRequest(Map<String, Object> paramMap, String url) {
        String result = "";
        try {
            String json = JSONObject.toJSONString(paramMap);
            // 只打印参数名不打印参数值：请求体含 signKey、患者身份证号、手机号等敏感信息，不应进日志
            log.info("--> 发送请求：url={}, params={}", url, paramMap.keySet());
            byte[] reqData = json.getBytes(StandardCharsets.UTF_8);
            byte[] respdata = HttpUtil.doPost(url, reqData, "application/json;charset=utf-8");
            result = new String(respdata, StandardCharsets.UTF_8);
            log.debug("--> 应答结果：{}", result);
        } catch (Exception ex) {
            log.error("HTTP请求失败，url: {}, error: {}", url, ex.getMessage(), ex);
            throw new YyghException("远程服务调用失败", ResultCodeEnum.FAIL.getCode());
        }
        if (result == null || result.isEmpty()) {
            log.error("HTTP请求返回空，url: {}", url);
            throw new YyghException("远程服务返回空", ResultCodeEnum.FAIL.getCode());
        }
        return JSONObject.parseObject(result);
    }
}
