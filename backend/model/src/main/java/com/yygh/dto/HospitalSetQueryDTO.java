package com.yygh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HospitalSetQueryDTO extends PageDTO {

    private String hosname;
    private String hoscode;
}
