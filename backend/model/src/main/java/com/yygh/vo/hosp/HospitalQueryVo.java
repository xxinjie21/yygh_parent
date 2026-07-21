package com.yygh.vo.hosp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 医院查询VO
 *
 * @author XXJ
 */
@Data
@Schema(description = "Hospital")
public class HospitalQueryVo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Schema(description = "医院编号")
	private String hoscode;

	@Schema(description = "医院名称")
	private String hosname;

	@Schema(description = "医院类型")
	private String hostype;

	@Schema(description = "省code")
	private String provinceCode;

	@Schema(description = "市code")
	private String cityCode;

	@Schema(description = "区code")
	private String districtCode;

	@Schema(description = "状态")
	private Integer status;
}

