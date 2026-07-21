package com.yygh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PageDTO {

    @Schema(description = "当前页码", example = "1")
    private Long page = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long size = 10L;
}
