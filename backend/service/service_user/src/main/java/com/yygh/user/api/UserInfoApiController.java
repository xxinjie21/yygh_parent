package com.yygh.user.api;

import com.yygh.common.result.Result;
import com.yygh.common.utils.AuthContextHolder;
import com.yygh.common.utils.BeanCopyUtils;
import com.yygh.user.service.UserInfoService;
import com.yygh.user.service.impl.UserInfoServiceImpl;
import com.yygh.vo.user.LoginVo;
import com.yygh.vo.user.UserAuthVo;
import com.yygh.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private final RedisTemplate<String, String> redisTemplate;

    /** 验证码的安全随机源 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    /** 同一手机号发送间隔（秒），防止短信轰炸 */
    private static final long SEND_INTERVAL_SECONDS = 60;
    /** 发送频率限制 key 前缀 */
    private static final String CODE_LIMIT_PREFIX = "sms:limit:";

    //发送手机验证码
    @Operation(summary = "发送验证码")
    @GetMapping("sendCode/{phone}")
    public Result sendCode(@PathVariable String phone) {
        // 发送频率限制：60秒内同一手机号只允许发送一次，防止短信轰炸
        String limitKey = CODE_LIMIT_PREFIX + phone;
        Boolean alreadySent = redisTemplate.hasKey(limitKey);
        if (Boolean.TRUE.equals(alreadySent)) {
            log.warn("验证码发送过于频繁，已拦截，phone={}", phone);
            return Result.fail().message("验证码发送过于频繁，请稍后再试");
        }
        // 使用安全随机源：java.util.Random 是线性同余算法，可通过历史输出推算后续值
        String code = String.valueOf(SECURE_RANDOM.nextInt(900000) + 100000);
        redisTemplate.opsForValue().set(UserInfoServiceImpl.SMS_CODE_KEY_PREFIX + phone, code,
                UserInfoServiceImpl.SMS_CODE_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(limitKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
        // 注意：绝不打印验证码明文，否则拿到日志即可登录任意账号
        return Result.ok();
    }

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
