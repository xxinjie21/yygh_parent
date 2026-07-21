package com.yygh.hosp.mapper;

import com.yygh.model.hosp.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
/**
 * 科室Mapper接口
 * @author XXJ
 */
public interface DepartmentMapper extends BaseMapper<Department> {

    Department selectByHoscodeAndDepcode(@Param("hoscode") String hoscode, @Param("depcode") String depcode);
}
