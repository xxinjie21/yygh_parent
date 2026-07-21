package com.yygh.user.controller;

import com.yygh.common.helper.JwtHelper;
import com.yygh.common.result.Result;
import com.yygh.common.utils.AuthContextHolder;
import com.yygh.dto.UserQueryDTO;
import com.yygh.user.service.UserInfoService;
import com.yygh.model.user.UserInfo;
import com.yygh.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器（后台管理）
 *
 * @author XXJ
 */
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
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getName, username);
        UserInfo userInfo = userInfoService.getOne(wrapper);
        if (userInfo == null) {
            return Result.fail().message("账号不存在");
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
