package com.yygh.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 订单MQ消息体
 *
 * @author XXJ
 */
@Data
@Schema(description = "OrderMqVo")
public class OrderMqVo {

	@Schema(description = "可预约数")
	private Integer reservedNumber;

	@Schema(description = "剩余预约数")
	private Integer availableNumber;

	@Schema(description = "排班id")
	private String scheduleId;

}

