package com.yygh.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PatientVo {

    private Long id;
    private Long userId;
    private String name;
    private String certificatesType;
    private String certificatesNo;
    private Integer sex;
    private String birthdate;
    private String phone;
    private String address;
    private String contactsName;
    private String contactsPhone;
    private String cardNo;
    private Integer isInsure;
    private Integer status;
}
