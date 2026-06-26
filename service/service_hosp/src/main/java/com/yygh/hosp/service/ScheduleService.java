package com.yygh.hosp.service;

import com.yygh.model.hosp.Schedule;
import com.yygh.vo.hosp.ScheduleOrderVo;
import com.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * 排班服务接口
 * @author XXJ
 */
public interface ScheduleService {
    // 上传排班接口
    void save(Map<String, Object> paramMap);

    // 查询排班接口（分页）
    IPage<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    // 删除排班接口
    void remove(String hoscode, String hosScheduleId);

    // 根据医院编号和科室编号，查询排班规则数据
    Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode);

    // 根据医院编号、科室编号和工作日期查询排班详细信息
    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

    // 获取可预约排班数据
    Object getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    // 根据排班id获取排班数据
    Schedule getScheduleId(String scheduleId);

    // 根据排班id获取预约下单数据
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    // 更新排班可预约数量（原子操作，供service_order内部调用）
    void updateAvailableNumber(String hosScheduleId, Integer delta);
}
