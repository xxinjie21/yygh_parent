package com.yygh.user.mapper;

import com.yygh.model.user.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息Mapper接口
 * @author XXJ
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
