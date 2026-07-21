package com.yygh.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserInfoVo {

    private Long id;
    private String openid;
    private String nickName;
    private String phone;
    private String name;
    private String certificatesType;
    private String certificatesNo;
    private Integer authStatus;
    private Integer status;
    private String createTime;
}
