package com.yygh.user.api;

import com.yygh.common.result.Result;
import com.yygh.common.utils.AuthContextHolder;
import com.yygh.common.utils.BeanCopyUtils;
import com.yygh.user.service.UserInfoService;
import com.yygh.vo.user.LoginVo;
import com.yygh.vo.user.UserAuthVo;
import com.yygh.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户信息API控制器
 *
 * @author XXJ
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserInfoApiController {

    private final UserInfoService userInfoService;

    //用户手机号登录接口
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
        Map<String, Object> info = userInfoService.loginUser(loginVo);
        return Result.ok(info);
    }

    //用户认证接口
    @PostMapping("auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, @RequestHeader("token") String token) {
        userInfoService.userAuth(AuthContextHolder.getUserId(token), userAuthVo);
        return Result.ok();
    }

    //获取用户id信息接口
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(@RequestHeader("token") String token) {
        Long userId = AuthContextHolder.getUserId(token);
        UserInfoVo userInfoVo = BeanCopyUtils.copy(userInfoService.getById(userId), UserInfoVo.class);
        return Result.ok(userInfoVo);
    }
}
