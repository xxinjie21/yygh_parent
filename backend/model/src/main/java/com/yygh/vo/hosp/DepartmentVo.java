package com.yygh.vo.hosp;

import com.yygh.model.hosp.Department;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 科室VO（树形结构）
 *
 * @author XXJ
 */
@Data
@Schema(description = "Department")
public class DepartmentVo {

	@Schema(description = "科室编号")
	private String depcode;

	@Schema(description = "科室名称")
	private String depname;

	@Schema(description = "下级节点")
	private List<DepartmentVo> children;

}

