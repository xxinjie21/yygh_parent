package com.yygh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PatientQueryDTO extends PageDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "证件类型")
    private String certificatesType;

    @Schema(description = "证件编号")
    private String certificatesNo;
}
