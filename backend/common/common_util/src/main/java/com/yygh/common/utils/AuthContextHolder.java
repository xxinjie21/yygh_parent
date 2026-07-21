package com.yygh.common.utils;



import com.yygh.common.helper.JwtHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户认证上下文持有者，从JWT token中获取当前登录用户信息
 *
 * @author XXJ
 */
@Slf4j
public class AuthContextHolder {
    /**
     * 获取用户ID
     * @param token JWT token
     * @return
     */
    public static Long getUserId(String token){
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }

    /**
     * 获取用户姓名
     * @param token JWT token
     * @return
     */
    public static String getUserName(String token){
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
}
