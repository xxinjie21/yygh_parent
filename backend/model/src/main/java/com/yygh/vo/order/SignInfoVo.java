package com.yygh.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 签名信息
 *
 * @author XXJ
 */
@Data
@Schema(description = "签名信息")
public class SignInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "api基础路径")
    private String apiUrl;

    @Schema(description = "签名秘钥")
    private String signKey;

}
