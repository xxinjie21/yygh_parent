package com.yygh.user.service;

import com.yygh.dto.UserQueryDTO;
import com.yygh.model.user.UserInfo;
import com.yygh.vo.user.LoginVo;
import com.yygh.vo.user.UserAuthVo;
import com.yygh.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    Map<String, Object> loginUser(LoginVo loginVo);
    UserInfo selectWxInfoOpenId(String openid);
    void userAuth(Long userId, UserAuthVo userAuthVo);
    IPage<UserInfoVo> selectPage(Page<UserInfo> pageParam, UserQueryDTO dto);
    void lock(Long userId, Integer status);
    UserInfoVo show(Long userId);
    void approval(Long userId, Integer authStatus);
}
