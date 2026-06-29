package com.yygh.vo.acl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 角色查询实体
 * </p>
 *
 * @author XXJ
 * @since 2019-11-08
 */
@Data
@Schema(description = "角色查询实体")
public class RoleQueryVo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Schema(description = "角色名称")
	private String roleName;

}

