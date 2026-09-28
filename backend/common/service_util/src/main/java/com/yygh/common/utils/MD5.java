package com.yygh.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * MD5摘要工具类
 *
 * @author XXJ
 */
public final class MD5 {

    /**
     * 计算字符串的MD5摘要
     *
     * <p><b>必须显式指定字符集</b>：此前使用 {@code strSrc.getBytes()} 依赖平台默认编码，
     * Windows（GBK）与 Linux（UTF-8）对中文的字节序列不同，
     * 会导致同一段含中文的参数在两端算出不同的签名，表现为「本地验签通过、部署到服务器全部失败」。
     *
     * @param strSrc 待摘要的字符串
     * @return 32位小写十六进制MD5值
     */
    public static String encrypt(String strSrc) {
        if (strSrc == null) {
            throw new IllegalArgumentException("MD5输入不能为空");
        }
        try {
            char hexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                    '9', 'a', 'b', 'c', 'd', 'e', 'f' };
            // 显式指定字符集，保证跨环境结果一致
            byte[] bytes = strSrc.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            bytes = md.digest();
            int j = bytes.length;
            char[] chars = new char[j * 2];
            int k = 0;
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                chars[k++] = hexChars[b >>> 4 & 0xf];
                chars[k++] = hexChars[b & 0xf];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("MD5加密出错！！+" + e);
        }
    }


}
