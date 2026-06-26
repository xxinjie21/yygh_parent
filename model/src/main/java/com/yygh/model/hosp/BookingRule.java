package com.yygh.model.hosp;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 预约规则（纯POJO，作为JSON嵌入hospital表的booking_rule字段）
 *
 * @author XXJ
 */
@Data
@ApiModel(description = "预约规则")
public class BookingRule {

    @ApiModelProperty(value = "预约周期")
    private Integer cycle;

    @ApiModelProperty(value = "放号时间")
    private String releaseTime;

    @ApiModelProperty(value = "停挂时间")
    private String stopTime;

    @ApiModelProperty(value = "退号截止天数（如：就诊前一天为-1，当天为0）")
    private Integer quitDay;

    @ApiModelProperty(value = "退号时间")
    private String quitTime;

    @ApiModelProperty(value = "预约规则")
    private List<String> rule;

    /**
     * 从JSON字符串设置预约规则列表
     */
    public void setRule(String rule) {
        if (!StringUtils.isEmpty(rule)) {
            this.rule = JSONArray.parseArray(rule, String.class);
        }
    }

}
