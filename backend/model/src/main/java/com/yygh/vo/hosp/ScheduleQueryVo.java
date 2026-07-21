package com.yygh.vo.hosp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 排班查询VO
 *
 * @author XXJ
 */
@Data
@Schema(description = "Schedule")
public class ScheduleQueryVo {
	
	@Schema(description = "医院编号")
	private String hoscode;

	@Schema(description = "科室编号")
	private String depcode;

	@Schema(description = "医生编号")
	private String doccode;

	@Schema(description = "安排日期")
	private Date workDate;

	@Schema(description = "安排时间（0：上午 1：下午）")
	private Integer workTime;

}

