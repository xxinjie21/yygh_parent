package com.yygh.hosp.mapper;

import com.yygh.model.hosp.Schedule;
import com.yygh.vo.hosp.BookingScheduleRuleVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
/**
 * 排班Mapper接口
 * @author XXJ
 */
public interface ScheduleMapper extends BaseMapper<Schedule> {

    Schedule selectByHoscodeAndHosScheduleId(@Param("hoscode") String hoscode, @Param("hosScheduleId") String hosScheduleId);

    List<Schedule> selectByHoscodeAndDepcodeAndWorkDate(@Param("hoscode") String hoscode, @Param("depcode") String depcode, @Param("workDate") Date workDate);

    /**
     * 按医院编号和科室编号分组，统计每个工作日的排班数据
     */
    List<BookingScheduleRuleVo> selectGroupByWorkDate(@Param("hoscode") String hoscode, @Param("depcode") String depcode);

    /**
     * 按医院编号、科室编号和指定日期列表分组统计
     */
    List<BookingScheduleRuleVo> selectGroupByWorkDateIn(@Param("hoscode") String hoscode, @Param("depcode") String depcode, @Param("dateList") List<Date> dateList);
}
