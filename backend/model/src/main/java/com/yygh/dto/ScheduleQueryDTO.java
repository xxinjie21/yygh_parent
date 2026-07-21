package com.yygh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleQueryDTO extends PageDTO {

    private String hoscode;
    private String depcode;
    private String doccode;
    private String workDate;
    private String workTime;
}
