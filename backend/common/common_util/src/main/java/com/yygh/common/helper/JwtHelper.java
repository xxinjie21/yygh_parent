package com.yygh.common.helper;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * JWT工具类，提供token的生成和解析功能
 *
 * <p><b>安全说明</b>：签名密钥应从环境变量 {@code JWT_SIGN_KEY} 或 JVM 系统属性 {@code jwt.sign.key} 注入。
 * 解析失败（过期、签名不符、被篡改）时统一返回 null，由调用方转换为 401，
 * 而不是抛出异常导致接口返回 500 —— 否则前端无法区分「未登录」与「服务器错误」。
 *
 * @author XXJ
 */
@Slf4j
public class JwtHelper {

    /** Token过期时间（24小时） */
    private static final long tokenExpiration = 24 * 60 * 60 * 1000;

    /** 签名密钥，从环境变量或系统属性读取 */
    private static final String tokenSignKey;

    static {
        String key = System.getenv("JWT_SIGN_KEY");
        if (key == null || key.isEmpty()) {
            key = System.getProperty("jwt.sign.key");
        }
        if (key == null || key.isEmpty()) {
            // 本地开发兜底值，启动时给出明确告警
            key = "yygh-local-dev-secret-please-override";
            log.warn("未检测到环境变量 JWT_SIGN_KEY / 系统属性 jwt.sign.key，已使用本地开发默认密钥。" +
                    "生产环境必须显式配置，否则一旦密钥泄露，攻击者可伪造 token 登录任意账号");
        }
        tokenSignKey = key;
    }

    /**
     * 根据参数生成token
     *
     * @param userId   用户id
     * @param userName 用户名
     * @return JWT字符串
     */
    public static String createToken(Long userId, String userName) {
        return Jwts.builder()
                .setSubject("YYGH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .claim("userId", userId)
                .claim("userName", userName)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
    }

    /**
     * 根据token字符串得到用户id
     *
     * @return 用户id；token 无效、过期或解析失败时返回 null
     */
    public static Long getUserId(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            return null;
        }
        Object userIdObj = claims.get("userId");
        // 兼容 Integer / Long：JSON 反序列化可能产出不同类型，直接强转会有 ClassCastException 风险
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        log.warn("JWT中userId格式异常");
        return null;
    }

    /**
     * 根据token字符串得到用户名称
     *
     * @return 用户名；token 无效时返回空字符串
     */
    public static String getUserName(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            return "";
        }
        Object userName = claims.get("userName");
        return userName == null ? "" : String.valueOf(userName);
    }

    /**
     * 解析JWT载荷，任何异常都被转换为 null，避免调用方收到 500
     */
    private static Claims parseClaims(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        try {
            return Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.debug("JWT已过期");
        } catch (SignatureException e) {
            log.warn("JWT签名校验失败，token可能被伪造");
        } catch (MalformedJwtException e) {
            log.warn("JWT格式非法");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT解析失败：{}", e.getMessage());
        }
        return null;
    }
}