package com.yygh.user.service.impl;

import com.yygh.cmn.client.DictFeignClient;
import com.yygh.common.exception.YyghException;
import com.yygh.common.helper.JwtHelper;
import com.yygh.common.result.ResultCodeEnum;

import com.yygh.dto.UserQueryDTO;
import com.yygh.enums.AuthStatusEnum;
import com.yygh.model.user.Patient;
import com.yygh.model.user.UserInfo;
import com.yygh.user.mapper.UserInfoMapper;
import com.yygh.user.service.PatientService;
import com.yygh.user.service.UserInfoService;

import com.yygh.vo.user.LoginVo;
import com.yygh.vo.user.UserAuthVo;
import com.yygh.vo.user.UserInfoVo;
import com.yygh.common.utils.BeanCopyUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 *
 * @author XXJ
 */
@RequiredArgsConstructor
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DictFeignClient dictFeignClient;
    private final PatientService patientService;

    //用户手机号登录接口
    @Override
    public Map<String, Object> loginUser(LoginVo loginVo) {
        //从loninVo获取输入的手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //判断手机号和验证码是否为空
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //手机号校验判断是否与缓存中的数据一致
        String codeInRedis = redisTemplate.opsForValue().get(phone);
        if (StringUtils.isEmpty(codeInRedis) || !codeInRedis.equals(code)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }

        //有值 说明用户是微信登陆了但没有绑定手机
        UserInfo userInfo = null;
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.selectWxInfoOpenId(loginVo.getOpenid());
            if (userInfo != null && userInfo.getPhone() != null) {
                userInfo.setPhone(loginVo.getPhone());
                this.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }
        //无值 说明用户非微信登录 现在判断该用户是否已用手机号登录
        if (userInfo == null) {
            //判断是否为第一次登录:根据手机号查询数据库,不存在则为第一次登录
            LambdaQueryWrapper<UserInfo> Wrapper = new LambdaQueryWrapper<>();
            Wrapper.eq(UserInfo::getPhone, phone);
            userInfo = baseMapper.selectOne(Wrapper);
            //不是第一次,直接登录
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setNickName("猫猫1号");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
        }

        //校验是否被禁用
        if (userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }
        //返回登录信息
        //返回登录用户名
        //返回token信息
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //jwt生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }

    /**
     * 判断用户数据是否存在 根据OpenID
     *
     * @param openid
     * @return
     */
    @Override
    public UserInfo selectWxInfoOpenId(String openid) {
        //条件构造查询用户
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getOpenid, openid);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        return userInfo;
    }

    //用户认证
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置认证信息
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //进行信息更新
        baseMapper.updateById(userInfo);
    }

    //用户列表（条件查询带分页）
    @Override
    public IPage<UserInfoVo> selectPage(Page<UserInfo> pageParam, UserQueryDTO dto) {
        String keyword = dto.getKeyword();
        Integer status = dto.getStatus();
        Integer authStatus = dto.getAuthStatus();
        String createTimeBegin = dto.getCreateTimeBegin();
        String createTimeEnd = dto.getCreateTimeEnd();
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(keyword != null, UserInfo::getName, keyword)
                .or().like(keyword != null, UserInfo::getPhone, keyword);
        wrapper.eq(status != null, UserInfo::getStatus, status);
        wrapper.eq(authStatus != null, UserInfo::getAuthStatus, authStatus);
        wrapper.ge(createTimeBegin != null, UserInfo::getCreateTime, createTimeBegin);
        wrapper.le(createTimeEnd != null, UserInfo::getCreateTime, createTimeEnd);
        IPage<UserInfo> rawPages = baseMapper.selectPage(pageParam, wrapper);
        rawPages.getRecords().forEach(this::packageUserInfo);
        List<UserInfoVo> voList = BeanCopyUtils.copyList(rawPages.getRecords(), UserInfoVo.class);
        IPage<UserInfoVo> result = new Page<>(rawPages.getCurrent(), rawPages.getSize(), rawPages.getTotal());
        result.setRecords(voList);
        return result;
    }

    //锁定
    @Override
    public void lock(Long userId, Integer status) {
        if (status.intValue() == 0 || status.intValue() == 1) {
            UserInfo userInfo = this.getById(userId);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }

    //用户详情
    @Override
    public UserInfoVo show(Long userId) {
        UserInfo userInfo = this.packageUserInfo(baseMapper.selectById(userId));
        return BeanCopyUtils.copy(userInfo, UserInfoVo.class);
    }

    //认证审批 2通过 -1不通过
    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus.intValue() == 2 || authStatus.intValue() == -1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

    //编号变成对应值封装
    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态 0  1
        String statusString = userInfo.getStatus().intValue() == 0 ? "锁定" : "正常";
        userInfo.getParam().put("statusString", statusString);
        return userInfo;
    }

}