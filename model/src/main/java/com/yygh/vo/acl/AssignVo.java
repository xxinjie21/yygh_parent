package com.yygh.vo.acl;

import lombok.Data;

/**
 * 分配权限VO
 *
 * @author XXJ
 */
@Data
public class AssignVo {

    private Long roleId;

    private Long[] permissionId;
}
