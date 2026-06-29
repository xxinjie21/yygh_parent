package com.yygh.model.user;

import com.yygh.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * UserLoginRecord
 * </p>
 *
 * @author XXJ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户登录日志")
@TableName("user_login_record")
public class UserLoginRecord extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	@Schema(description = "用户id")
	@TableField("user_id")
	private Long userId;

	@Schema(description = "ip")
	@TableField("ip")
	private String ip;

}

