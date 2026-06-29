package com.yygh.hospital.dto;

import lombok.Data;

@Data
public class SubmitOrderDTO {
    private String hoscode;
    private String depcode;
    private String hosScheduleId;
    private String reserveDate;
    private String reserveTime;
    private String amount;
    private String name;
    private String certificatesType;
    private String certificatesNo;
    private String sex;
    private String birthdate;
    private String phone;
    private String isMarry;
    private String provinceCode;
    private String cityCode;
    private String districtCode;
    private String address;
    private String contactsName;
    private String contactsCertificatesType;
    private String contactsCertificatesNo;
    private String contactsPhone;
    private String timestamp;
    private String sign;
}
