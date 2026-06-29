package com.yygh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageDTO {

    private String keyword;
    private Integer status;
    private Integer authStatus;
    private String createTimeBegin;
    private String createTimeEnd;
}
