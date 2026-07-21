package com.yygh.vo.hosp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HospitalVo {

    private String id;
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
    private Integer status;
    private String bookingRule;
}
