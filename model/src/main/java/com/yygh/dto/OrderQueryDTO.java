package com.yygh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQueryDTO extends PageDTO {

    private Long userId;
    private String outTradeNo;
    private Long patientId;
    private String patientName;
    private String keyword;
    private String orderStatus;
    private String reserveDate;
    private String createTimeBegin;
    private String createTimeEnd;
}
