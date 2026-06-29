package com.yygh.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 订单统计VO
 *
 * @author XXJ
 */
@Data
@Schema(description = "OrderCountVo")
public class OrderCountVo {

	@Schema(description = "安排日期")
	private String reserveDate;

	@Schema(description = "预约单数")
	private Integer count;

	@Schema(description = "日期列表")
	private List<String> dateList;

	@Schema(description = "数量列表")
	private List<Integer> countList;

}

