package com.yygh.model.hosp;

import com.yygh.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 科室信息
 *
 * @author XXJ
 */
@Data
@ApiModel(description = "科室信息")
@TableName("department")
public class Department extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "医院编号")
    @TableField("hoscode")
    private String hoscode;

    @ApiModelProperty(value = "科室编号")
    @TableField("depcode")
    private String depcode;

    @ApiModelProperty(value = "科室名称")
    @TableField("depname")
    private String depname;

    @ApiModelProperty(value = "科室描述")
    @TableField("intro")
    private String intro;

    @ApiModelProperty(value = "大科室编号")
    @TableField("bigcode")
    private String bigcode;

    @ApiModelProperty(value = "大科室名称")
    @TableField("bigname")
    private String bigname;

}
