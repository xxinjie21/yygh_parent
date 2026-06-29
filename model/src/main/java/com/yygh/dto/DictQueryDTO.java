package com.yygh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictQueryDTO extends PageDTO {

    @Schema(description = "上级ID")
    private Long parentId;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "名称")
    private String name;
}
