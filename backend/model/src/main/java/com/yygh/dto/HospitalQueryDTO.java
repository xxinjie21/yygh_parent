package com.yygh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HospitalQueryDTO extends PageDTO {

    private String hoscode;
    private String hosname;
    private String hostype;
    private String provinceCode;
    private String cityCode;
    private String districtCode;
    private Integer status;
}
