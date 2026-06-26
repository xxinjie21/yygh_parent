package com.yygh.model.hosp;

import com.yygh.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 排班信息
 *
 * @author XXJ
 */
@Data
@ApiModel(description = "排班信息")
@TableName("schedule")
public class Schedule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "医院编号")
    @TableField("hoscode")
    private String hoscode;

    @ApiModelProperty(value = "科室编号")
    @TableField("depcode")
    private String depcode;

    @ApiModelProperty(value = "职称")
    @TableField("title")
    private String title;

    @ApiModelProperty(value = "医生名称")
    @TableField("docname")
    private String docname;

    @ApiModelProperty(value = "擅长技能")
    @TableField("skill")
    private String skill;

    @ApiModelProperty(value = "排班日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("work_date")
    private Date workDate;

    @ApiModelProperty(value = "排班时间（0：上午 1：下午）")
    @TableField("work_time")
    private Integer workTime;

    @ApiModelProperty(value = "可预约数")
    @TableField("reserved_number")
    private Integer reservedNumber;

    @ApiModelProperty(value = "剩余预约数")
    @TableField("available_number")
    private Integer availableNumber;

    @ApiModelProperty(value = "挂号费")
    @TableField("amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "排班状态（-1：停诊 0：停约 1：可约）")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "排班编号（医院自己的排班主键）")
    @TableField("hos_schedule_id")
    private String hosScheduleId;

}
