package com.yygh.vo.hosp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 科室查询VO
 *
 * @author XXJ
 */
@Data
@Schema(description = "Department")
public class DepartmentQueryVo {
	
	@Schema(description = "医院编号")
	private String hoscode;

	@Schema(description = "科室编号")
	private String depcode;

	@Schema(description = "科室名称")
	private String depname;

	@Schema(description = "大科室编号")
	private String bigcode;

	@Schema(description = "大科室名称")
	private String bigname;

}

