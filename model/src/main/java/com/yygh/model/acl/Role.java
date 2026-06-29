package com.yygh.model.acl;

import com.yygh.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 角色
 * </p>
 *
 * @author XXJ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色")
@TableName("acl_role")
public class Role extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	@Schema(description = "角色名称")
	@TableField("role_name")
	private String roleName;

	@Schema(description = "备注")
	@TableField("remark")
	private String remark;

}

