package com.yygh.dto;

import lombok.Data;

@Data
public class ScheduleSaveDTO {
    private String hoscode;
    private String hosScheduleId;
    private String depcode;
    private String title;
    private String docname;
    private String skill;
    private String workDate;
    private String workTime;
    private Integer reservedNumber;
    private Integer availableNumber;
    private String amount;
    private String status;
    private String sign;
}
