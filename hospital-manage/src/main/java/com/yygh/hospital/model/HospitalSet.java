package com.yygh.hospital.model;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * HospitalSet
 * </p>
 *
 * @author XXJ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "HospitalSet")
@TableName("hospital_set")
public class HospitalSet extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	@Schema(description = "医院编号")
	private String hoscode;

	@Schema(description = "签名秘钥")
	private String signKey;

	@Schema(description = "api基础路径")
	private String apiUrl;

}

