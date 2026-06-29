package com.yygh.user.api;

import com.alibaba.fastjson.JSONObject;
import com.yygh.common.exception.YyghException;
import com.yygh.common.helper.JwtHelper;
import com.yygh.common.result.Result;
import com.yygh.common.result.ResultCodeEnum;
import com.yygh.model.user.UserInfo;
import com.yygh.user.service.UserInfoService;
import com.yygh.user.utils.ConstantWxPropertiesUtils;
import com.yygh.user.utils.HttpClientUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信操作控制器
 * @author XXJ
 */
//微信操作接口
@Controller
@RequestMapping("/api/ucenter/wx")
@RequiredArgsConstructor
public class WxApiController {
    private final UserInfoService userInfoService;
    private final RedisTemplate redisTemplate;

    //1 生成微信扫描二维码
    //返回生成二维码需要参数
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result genQrConnect() throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(ConstantWxPropertiesUtils.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantWxPropertiesUtils.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis() + "");//System.currentTimeMillis()+""
        return Result.ok(map);
    }

    /**
     * 微信登录回调
     *
     * @param code
     * @param state
     * @return
     */
    @RequestMapping("callback")
    public String callback(String code, String state) {
        //第一步 获取临时票据 code
        System.out.println("code:" + code);
        //第二步 拿着code和微信id和秘钥，请求微信固定地址 ，得到两个值
        //使用code和appid以及appscrect换取access_token
        //  %s   占位符
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantWxPropertiesUtils.WX_OPEN_APP_ID,
                ConstantWxPropertiesUtils.WX_OPEN_APP_SECRET,
                code);
        //使用httpclient请求这个地址
        try {
            //请求获取地址
            String accesstokenInfo = HttpClientUtils.get(accessTokenUrl);
            System.out.println("accesstokenInfo:" + accesstokenInfo);
            //从返回字符串获取两个值 openid和access_token   转成JSON获取请求数据和微信标识
            JSONObject jsonObject = JSONObject.parseObject(accesstokenInfo);
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");

            //判断数据库是否存在微信的扫描人信息
            //根据openid判断
            UserInfo userInfo = userInfoService.selectWxInfoOpenId(openid);
            //数据库不存在微信信息
            if(userInfo == null) {
                //第三步 拿着openid  和  access_token请求微信地址，得到扫描人信息  httpclient
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";
                String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
                String resultInfo = HttpClientUtils.get(userInfoUrl);
                //把扫码人信息转成JSON
                System.out.println("resultInfo:" + resultInfo);
                JSONObject resultUserInfoJson = JSONObject.parseObject(resultInfo);
                //解析用户信息
                //用户昵称
                String nickname = resultUserInfoJson.getString("nickname");
                //用户头像
                String headimgurl = resultUserInfoJson.getString("headimgurl");
//            获取扫描人信息添加数据库
                userInfo = new UserInfo();
                userInfo.setNickName(nickname);
                userInfo.setOpenid(openid);
                userInfo.setStatus(1);
                userInfoService.save(userInfo);
            }else {
                state="2";
            }
            //返回name和token字符串
            Map<String,String> map = new HashMap<>();
            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            map.put("name", name);
            //判断userInfo是否有手机号，如果手机号为空，返回openid
            //如果手机号不为空，返回openid值是空字符串
            //前端判断：如果openid不为空，绑定手机号，如果openid为空，不需要绑定手机号
            if(StringUtils.isEmpty(userInfo.getPhone())) {
                map.put("openid", userInfo.getOpenid());
            } else {
                map.put("openid", "null");
            }
            //使用jwt生成token字符串
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token", token);
            //打印一下open id
            System.out.println("openID"+map.get("openid"));
            //跳转到前端页面
            return "redirect:" +
                    ConstantWxPropertiesUtils.YYGH_BASE_URL +
                    "/weixin/callback?token="+map.get("token")+ "&openid="+map.get("openid")+"&name="
                    +URLEncoder.encode(map.get("name"),"utf-8")+"&state="+state;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
