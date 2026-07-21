package com.yygh.vo.hosp;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 医院设置查询VO
 *
 * @author XXJ
 */
@Data
public class HospitalSetQueryVo {

    @Schema(description = "医院名称")
    private String hosname;

    @Schema(description = "医院编号")
    private String hoscode;
}
