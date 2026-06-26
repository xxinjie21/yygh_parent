package com.yygh.model.hosp;

import com.alibaba.fastjson.JSONObject;
import com.yygh.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 医院信息
 *
 * @author XXJ
 */
@Data
@ApiModel(description = "医院信息")
@TableName("hospital")
public class Hospital extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "医院编号")
    @TableField("hoscode")
    private String hoscode;

    @ApiModelProperty(value = "医院名称")
    @TableField("hosname")
    private String hosname;

    @ApiModelProperty(value = "医院类型")
    @TableField("hostype")
    private String hostype;

    @ApiModelProperty(value = "省code")
    @TableField("province_code")
    private String provinceCode;

    @ApiModelProperty(value = "市code")
    @TableField("city_code")
    private String cityCode;

    @ApiModelProperty(value = "区code")
    @TableField("district_code")
    private String districtCode;

    @ApiModelProperty(value = "详情地址")
    @TableField("address")
    private String address;

    @ApiModelProperty(value = "医院logo")
    @TableField("logo_data")
    private String logoData;

    @ApiModelProperty(value = "医院简介")
    @TableField("intro")
    private String intro;

    @ApiModelProperty(value = "坐车路线")
    @TableField("route")
    private String route;

    @ApiModelProperty(value = "状态 0：未上线 1：已上线")
    @TableField("status")
    private Integer status;

    /**
     * 预约规则（JSON字符串存储）
     * 数据库中存JSON字符串，通过getter/setter自动转换
     */
    @ApiModelProperty(value = "预约规则")
    @TableField("booking_rule")
    private String bookingRule;

    /**
     * 获取预约规则对象
     */
    public BookingRule getBookingRule() {
        if (this.bookingRule != null) {
            return JSONObject.parseObject(this.bookingRule, BookingRule.class);
        }
        return null;
    }

    /**
     * 设置预约规则（从字符串反序列化）
     */
    public void setBookingRule(String bookingRule) {
        this.bookingRule = bookingRule;
    }

}
