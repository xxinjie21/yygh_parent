package com.yygh.user.service;

import com.yygh.model.user.UserInfo;
import com.yygh.vo.user.LoginVo;
import com.yygh.vo.user.UserAuthVo;
import com.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

/**
 * 用户信息服务接口
 * @author XXJ
 */
public interface UserInfoService extends IService<UserInfo> {
    //用户手机号登录接口
    Map<String, Object> loginUser(LoginVo loginVo);
    UserInfo selectWxInfoOpenId(String openid);
    //用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);
    //用户列表（条件查询带分页）
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);
    //锁定
    void lock(Long userId, Integer status);
    //用户详情
    Map<String, Object> show(Long userId);
    //认证审批
    void approval(Long userId, Integer authStatus);
}
