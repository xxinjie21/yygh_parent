package com.yygh.vo.hosp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HospitalSetVo {

    private Long id;
    private String hosname;
    private String hoscode;
    private String apiUrl;
    private String signKey;
    private String contactsName;
    private String contactsPhone;
    private Integer status;
}
