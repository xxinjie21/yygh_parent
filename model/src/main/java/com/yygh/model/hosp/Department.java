package com.yygh.model.hosp;

import com.yygh.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 科室信息
 *
 * @author XXJ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "科室信息")
@TableName("department")
public class Department extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "医院编号")
    @TableField("hoscode")
    private String hoscode;

    @Schema(description = "科室编号")
    @TableField("depcode")
    private String depcode;

    @Schema(description = "科室名称")
    @TableField("depname")
    private String depname;

    @Schema(description = "科室描述")
    @TableField("intro")
    private String intro;

    @Schema(description = "大科室编号")
    @TableField("bigcode")
    private String bigcode;

    @Schema(description = "大科室名称")
    @TableField("bigname")
    private String bigname;

}
