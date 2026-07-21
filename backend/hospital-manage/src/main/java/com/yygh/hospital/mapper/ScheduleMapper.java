package com.yygh.hospital.mapper;

import com.yygh.hospital.model.Schedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 排班信息Mapper接口
 *
 * @author XXJ
 */
@Mapper
@Repository
public interface ScheduleMapper extends BaseMapper<Schedule> {

}
