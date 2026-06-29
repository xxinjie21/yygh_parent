package com.yygh.model.hosp;

import com.yygh.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 排班信息
 *
 * @author XXJ
 */
@Data
@Schema(description = "排班信息")
@TableName("schedule")
public class Schedule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "医院编号")
    @TableField("hoscode")
    private String hoscode;

    @Schema(description = "科室编号")
    @TableField("depcode")
    private String depcode;

    @Schema(description = "职称")
    @TableField("title")
    private String title;

    @Schema(description = "医生名称")
    @TableField("docname")
    private String docname;

    @Schema(description = "擅长技能")
    @TableField("skill")
    private String skill;

    @Schema(description = "排班日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("work_date")
    private Date workDate;

    @Schema(description = "排班时间（0：上午 1：下午）")
    @TableField("work_time")
    private Integer workTime;

    @Schema(description = "可预约数")
    @TableField("reserved_number")
    private Integer reservedNumber;

    @Schema(description = "剩余预约数")
    @TableField("available_number")
    private Integer availableNumber;

    @Schema(description = "挂号费")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "排班状态（-1：停诊 0：停约 1：可约）")
    @TableField("status")
    private Integer status;

    @Schema(description = "排班编号（医院自己的排班主键）")
    @TableField("hos_schedule_id")
    private String hosScheduleId;

}
