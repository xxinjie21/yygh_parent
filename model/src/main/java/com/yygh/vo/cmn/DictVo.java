package com.yygh.vo.cmn;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DictVo {

    private Long id;
    private Long parentId;
    private String name;
    private Long value;
    private String dictCode;
}
