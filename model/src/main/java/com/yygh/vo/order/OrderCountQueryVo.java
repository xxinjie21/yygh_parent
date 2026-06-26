package com.yygh.vo.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单统计查询VO
 *
 * @author XXJ
 */
@Data
@ApiModel(description = "OrderCountQueryVo")
public class OrderCountQueryVo {
	
	@ApiModelProperty(value = "医院编号")
	private String hoscode;

	@ApiModelProperty(value = "医院名称")
	private String hosname;

	@ApiModelProperty(value = "安排日期")
	private String reserveDateBegin;
	private String reserveDateEnd;

}

