package com.yygh.vo.user;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户认证VO
 *
 * @author XXJ
 */
@Data
@Schema(description = "会员认证对象")
public class UserAuthVo {

    @Schema(description = "用户姓名")
    @TableField("name")
    private String name;

    @Schema(description = "证件类型")
    @TableField("certificates_type")
    private String certificatesType;

    @Schema(description = "证件编号")
    @TableField("certificates_no")
    private String certificatesNo;

    @Schema(description = "证件路径")
    @TableField("certificates_url")
    private String certificatesUrl;

}
