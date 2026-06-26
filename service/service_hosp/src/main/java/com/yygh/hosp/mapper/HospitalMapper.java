package com.yygh.hosp.mapper;

import com.yygh.model.hosp.Hospital;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
/**
 * 医院Mapper接口
 * @author XXJ
 */
public interface HospitalMapper extends BaseMapper<Hospital> {

    Hospital selectByHoscode(@Param("hoscode") String hoscode);

    List<Hospital> selectByHosnameLike(@Param("hosname") String hosname);
}
