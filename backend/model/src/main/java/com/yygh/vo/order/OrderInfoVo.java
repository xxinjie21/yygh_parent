package com.yygh.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderInfoVo {

    private Long id;
    private Long userId;
    private String outTradeNo;
    private String hoscode;
    private String hosname;
    private String depcode;
    private String depname;
    private String title;
    private String hosScheduleId;
    private String reserveDate;
    private Integer reserveTime;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String hosRecordId;
    private Integer number;
    private String fetchTime;
    private String fetchAddress;
    private BigDecimal amount;
    private Integer orderStatus;
    private String createTime;
}
