package com.yygh.model.acl;

import com.yygh.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 权限
 * </p>
 *
 * @author XXJ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "权限")
@TableName("acl_permission")
public class Permission extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	@Schema(description = "所属上级")
	@TableField("pid")
	private Long pid;

	@Schema(description = "名称")
	@TableField("name")
	private String name;

	@Schema(description = "类型(1:菜单,2:按钮)")
	@TableField("type")
	private Integer type;

	@Schema(description = "权限值")
	@TableField("permission_value")
	private String permissionValue;

	@Schema(description = "路径")
	@TableField("path")
	private String path;

	@Schema(description = "component")
	@TableField("component")
	private String component;

	@Schema(description = "图标")
	@TableField("icon")
	private String icon;

	@Schema(description = "状态(0:禁止,1:正常)")
	@TableField("status")
	private Integer status;

	@Schema(description = "层级")
	@TableField(exist = false)
	private Integer level;

	@Schema(description = "下级")
	@TableField(exist = false)
	private List<Permission> children;

	@Schema(description = "是否选中")
	@TableField(exist = false)
	private boolean isSelect;

}

