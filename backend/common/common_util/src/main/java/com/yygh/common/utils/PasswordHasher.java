package com.yygh.common.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * 口令哈希工具：PBKDF2WithHmacSHA256 + 随机盐
 *
 * <p>为什么不能用 MD5 / SHA 直接存 password：
 * 这类通用摘要算法速度极快，攻击者可用彩虹表和 GPU 暴力碰撞反推明文。
 * PBKDF2 通过<b>高迭代次数</b>刻意放慢计算速度，并为每个口令生成<b>独立随机盐</b>，
 * 使彩虹表失效。JDK 原生支持，无需引入 Spring Security 等额外依赖。
 *
 * <p>存储格式：{@code pbkdf2$<迭代次数>$<Base64盐>$<Base64哈希>}
 *
 * @author XXJ
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2";
    private static final String SEPARATOR = "\\$";
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /**
     * 生成口令哈希
     *
     * @param rawPassword 明文口令
     * @return 带盐与迭代次数的哈希串，可直接入库
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("口令不能为空");
        }
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword, salt, ITERATIONS, KEY_LENGTH);
        return PREFIX + "$" + ITERATIONS + "$" + encode(salt) + "$" + encode(hash);
    }

    /**
     * 校验明文口令是否与存储的哈希匹配
     *
     * @param rawPassword 明文口令
     * @param stored      {@link #hash(String)} 生成的哈希串
     * @return true 表示匹配
     */
    public static boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null) {
            return false;
        }
        String[] parts = stored.split(SEPARATOR);
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(rawPassword, salt, Integer.parseInt(parts[1]), expected.length * 8);
            // 恒定时间比较，避免通过响应耗时差异逐字节试探
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 判断给定字符串是否为合法的 PBKDF2 哈希格式
     */
    public static boolean isHashed(String value) {
        if (value == null) {
            return false;
        }
        String[] parts = value.split(SEPARATOR);
        return parts.length == 4 && PREFIX.equals(parts[0]);
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("口令哈希计算失败", e);
        }
    }

    private static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
}
