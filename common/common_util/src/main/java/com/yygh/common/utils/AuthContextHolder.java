package com.yygh.common.utils;



import com.yygh.common.helper.JwtHelper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户认证上下文持有者，从请求中获取当前登录用户信息
 *
 * @author XXJ
 */
@Slf4j
public class AuthContextHolder {
    /**
     * 获取用户ID 实名认证
     * @param request
     * @return
     */
    public static Long getUserId(HttpServletRequest request){
        //头中获取token 用于判断用户是否登录状态
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }

    /**
     * 获取用户姓名 实名认证
     * @param request
     * @return
     */
    public static  String getUserName(HttpServletRequest request){
        //头中获取token 用于判断用户是否登录状态
        String token = request.getHeader("token");
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
}
