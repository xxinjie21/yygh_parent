package com.yygh.user.controller;

import com.yygh.common.helper.JwtHelper;
import com.yygh.common.result.Result;
import com.yygh.common.utils.AuthContextHolder;
import com.yygh.common.utils.PasswordHasher;
import com.yygh.dto.UserQueryDTO;
import com.yygh.user.service.UserInfoService;
import com.yygh.model.user.UserInfo;
import com.yygh.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器（后台管理）
 *
 * @author XXJ
 */
@Slf4j
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserController {
    private final UserInfoService userInfoService;

    //管理员登录
    @PostMapping("login")
    public Result login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return Result.fail().message("用户名或密码不能为空");
        }
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getName, username);
        UserInfo userInfo = userInfoService.getOne(wrapper);
        if (userInfo == null) {
            return Result.fail().message("账号不存在");
        }
        // 口令校验：此前只判断账号是否存在就签发 token，
        // 任何知道用户名的人都能直接登录后台，属于严重的认证缺失
        if (!PasswordHasher.matches(password, userInfo.getPassword())) {
            log.warn("管理员登录失败，口令不匹配，账号：{}", username);
            return Result.fail().message("用户名或密码错误");
        }
        if (userInfo.getStatus() != null && userInfo.getStatus() == 0) {
            return Result.fail().message("账号已被锁定");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("token", JwtHelper.createToken(userInfo.getId(), userInfo.getName()));
        return Result.ok(map);
    }

    //获取管理员信息
    @GetMapping("info")
    public Result info(@RequestParam("token") String token) {
        Long userId = AuthContextHolder.getUserId(token);
        UserInfo userInfo = userInfoService.getById(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("name", userInfo != null ? userInfo.getName() : "admin");
        map.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        return Result.ok(map);
    }

    //管理员登出
    @PostMapping("logout")
    public Result logout() {
        return Result.ok();
    }

    //用户列表（条件查询带分页）
    @PostMapping("list")
    public Result list(@RequestBody UserQueryDTO dto) {
        Page<UserInfo> pageParam = new Page<>(dto.getPage(), dto.getSize());
        IPage<UserInfoVo> pageModel = userInfoService.selectPage(pageParam, dto);
        return Result.ok(pageModel);
    }

    //锁定
    @GetMapping("lock/{userId}/{status}")
    public Result lock(
            @PathVariable("userId") Long userId,
            @PathVariable("status") Integer status) {
        userInfoService.lock(userId, status);
        return Result.ok();
    }

    //用户详情
    @GetMapping("show/{userId}")
    public Result show(@PathVariable Long userId) {
        UserInfoVo vo = userInfoService.show(userId);
        return Result.ok(vo);
    }

    //认证审批
    @GetMapping("approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId, @PathVariable Integer authStatus) {
        userInfoService.approval(userId, authStatus);
        return Result.ok();
    }
}
