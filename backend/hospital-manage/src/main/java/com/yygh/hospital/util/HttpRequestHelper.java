package com.yygh.hospital.util;

import com.alibaba.fastjson.JSONObject;
import com.yygh.common.exception.YyghException;
import com.yygh.common.result.ResultCodeEnum;
import com.yygh.common.utils.HttpUtil;
import com.yygh.common.utils.MD5;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * HTTP请求辅助工具类，提供签名生成、签名校验、请求发送等功能
 *
 * @author XXJ
 */
@Slf4j
public class HttpRequestHelper {

    /**
     * 请求数据获取签名
     *
     * <p><b>安全说明</b>：严禁打印参与签名的原始串，其中包含 signKey 等敏感凭据，
     * 写入日志等同于密钥泄露。
     *
     * @param paramMap 参与签名的参数（本方法会移除其中已有的 sign 键）
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
     * 签名校验
     *
     * @return true 表示签名一致；缺少 sign 参数时返回 false，不抛异常
     */
    public static boolean isSignEquals(Map<String, Object> paramMap, String signKey) {
        String sign = (String) paramMap.get("sign");
        if (sign == null || sign.isEmpty()) {
            log.warn("请求缺少 sign 参数，签名校验失败");
            return false;
        }
        String md5Str = getSign(paramMap, signKey);
        // 使用恒定时间比较，降低时序攻击风险
        return MessageDigest.isEqual(sign.getBytes(StandardCharsets.UTF_8),
                md5Str.getBytes(StandardCharsets.UTF_8));
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
            // 只打印参数名不打印参数值：请求体含患者身份证号、手机号等敏感信息，不应进入日志
            log.info("--> 发送请求：url={}, params={}", url, paramMap.keySet());
            byte[] reqData = json.getBytes(StandardCharsets.UTF_8);
            byte[] respdata = HttpUtil.doPost(url, reqData, "application/json;charset=utf-8");
            result = new String(respdata, StandardCharsets.UTF_8);
            log.info("--> 应答结果：{}", result);
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
