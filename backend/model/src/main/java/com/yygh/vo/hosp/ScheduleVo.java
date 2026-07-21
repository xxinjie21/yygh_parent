package com.yygh.vo.hosp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScheduleVo {

    private String id;
    private String hoscode;
    private String depcode;
    private String title;
    private String docname;
    private String skill;
    private String workDate;
    private Integer workTime;
    private Integer reservedNumber;
    private Integer availableNumber;
    private Integer status;
}
