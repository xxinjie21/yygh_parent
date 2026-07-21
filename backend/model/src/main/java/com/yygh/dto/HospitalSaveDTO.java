package com.yygh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HospitalSaveDTO {
    private String hoscode;
    private String hosname;
    private String hostype;
    private String provinceCode;
    private String cityCode;
    private String districtCode;
    private String address;
    private String logoData;
    private String intro;
    private String route;
    private String status;
    private String bookingRule;
    private String sign;
}
