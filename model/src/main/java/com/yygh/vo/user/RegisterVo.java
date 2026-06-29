package com.yygh.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 注册VO
 *
 * @author XXJ
 */
@Data
@Schema(description = "注册对象")
public class RegisterVo {

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "验证码")
    private String code;
}
