package com.yygh.user.controller;

import com.yygh.common.result.Result;
import com.yygh.dto.UserQueryDTO;
import com.yygh.user.service.UserInfoService;
import com.yygh.model.user.UserInfo;
import com.yygh.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
