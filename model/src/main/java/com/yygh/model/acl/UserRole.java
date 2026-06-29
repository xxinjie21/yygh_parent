package com.yygh.model.acl;

import com.yygh.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 用户角色
 * </p>
 *
 * @author XXJ
 * @since 2019-11-08
 */
@Data
@Schema(description = "用户角色")
@TableName("acl_user_role")
public class UserRole extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	@Schema(description = "角色id")
	@TableField("role_id")
	private Long roleId;

	@Schema(description = "用户id")
	@TableField("user_id")
	private Long userId;

}

