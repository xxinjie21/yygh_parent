package com.yygh.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单统计查询VO
 *
 * @author XXJ
 */
@Data
@Schema(description = "OrderCountQueryVo")
public class OrderCountQueryVo {
	
	@Schema(description = "医院编号")
	private String hoscode;

	@Schema(description = "医院名称")
	private String hosname;

	@Schema(description = "安排日期")
	private String reserveDateBegin;
	private String reserveDateEnd;

}

