package com.yygh.user.mapper;

import com.yygh.model.user.Patient;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 就诊人Mapper接口
 * @author XXJ
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
}
