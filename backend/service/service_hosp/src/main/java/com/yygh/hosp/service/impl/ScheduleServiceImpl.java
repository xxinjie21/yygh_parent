package com.yygh.hosp.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.yygh.common.exception.YyghException;
import com.yygh.common.result.ResultCodeEnum;
import com.yygh.common.utils.BeanCopyUtils;
import com.yygh.hosp.mapper.HospitalMapper;
import com.yygh.hosp.mapper.ScheduleMapper;
import com.yygh.hosp.service.DepartmentService;
import com.yygh.hosp.service.HospitalService;
import com.yygh.hosp.service.ScheduleService;
import com.yygh.model.hosp.BookingRule;
import com.yygh.model.hosp.Department;
import com.yygh.model.hosp.Hospital;
import com.yygh.model.hosp.Schedule;
import com.yygh.vo.hosp.BookingScheduleRuleVo;
import com.yygh.vo.hosp.ScheduleOrderVo;
import com.yygh.vo.hosp.ScheduleVo;
import com.yygh.dto.ScheduleQueryDTO;
import com.yygh.dto.ScheduleSaveDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 排班服务实现类
 * @author XXJ
 */
@RequiredArgsConstructor
@Service
public class ScheduleServiceImpl extends
        ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {
    /**
     * 显式声明 slf4j 日志对象，遮蔽父类 ServiceImpl 继承来的 ibatis Log。
     *
     * <p>为什么不能直接依赖 @Slf4j：MyBatis-Plus 的 ServiceImpl 中有一个
     * <code>protected final org.apache.ibatis.logging.Log log</code> 字段，
     * Lombok 发现父类已存在同名 log 时会跳过生成，导致此处的 log 是 ibatis 的 Log 实现，
     * 它没有 info(String, Object...) 这类占位符重载，使用占位符打日志会直接编译失败。
     */
    private static final Logger log = LoggerFactory.getLogger(ScheduleServiceImpl.class);


    private final ScheduleMapper scheduleMapper;

    private final HospitalMapper hospitalMapper;

    private final HospitalService hospitalService;

    private final DepartmentService departmentService;

    /**
     * 上传排班数据
     *
     * <p>排班新增或更新后必须清空 {@code schedule} 缓存，
     * 否则 {@link #getDetailSchedule} 会一直返回旧的号源余量。
     */
    @Override
    @CacheEvict(value = "schedule", allEntries = true)
    public void save(ScheduleSaveDTO scheduleSaveDTO) {
        String dtoString = JSONObject.toJSONString(scheduleSaveDTO);
        Schedule schedule = JSONObject.parseObject(dtoString, Schedule.class);

        // 根据医院编号和排班编号查询是否已存在
        Schedule scheduleExist = scheduleMapper.selectByHoscodeAndHosScheduleId(
                schedule.getHoscode(), schedule.getHosScheduleId());
        if (scheduleExist != null) {
            // 已存在则更新
            scheduleExist.setUpdateTime(new Date());
            scheduleExist.setIsDeleted(0);
            // 复制新数据到已存在记录
            BeanUtils.copyProperties(schedule, scheduleExist, "id", "createTime");
            scheduleExist.setUpdateTime(new Date());
            scheduleExist.setIsDeleted(0);
            baseMapper.updateById(scheduleExist);
            log.info("排班数据更新成功，医院编号：{}，排班编号：{}", schedule.getHoscode(), schedule.getHosScheduleId());
        } else {
            // 不存在则新增
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            baseMapper.insert(schedule);
            log.info("排班数据新增成功，医院编号：{}，排班编号：{}", schedule.getHoscode(), schedule.getHosScheduleId());
        }
    }

    // 查询排班（分页 + 条件查询）
    @Override
    public IPage<Schedule> findPageSchedule(ScheduleQueryDTO dto) {
        // 构建分页对象
        Page<Schedule> pageParam = new Page<>(dto.getPage(), dto.getSize());
        // 构建查询条件
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getIsDeleted, 0);
        wrapper.eq(Schedule::getStatus, 1);
        // 根据查询条件动态添加
        if (dto != null) {
            if (!StringUtils.isEmpty(dto.getHoscode())) {
                wrapper.eq(Schedule::getHoscode, dto.getHoscode());
            }
            if (!StringUtils.isEmpty(dto.getDepcode())) {
                wrapper.eq(Schedule::getDepcode, dto.getDepcode());
            }
        }
        IPage<Schedule> pages = baseMapper.selectPage(pageParam, wrapper);
        return pages;
    }

    // 删除排班
    @Override
    @CacheEvict(value = "schedule", allEntries = true)
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleMapper.selectByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (schedule != null) {
            baseMapper.deleteById(schedule.getId());
            log.info("排班数据删除成功，医院编号：{}，排班编号：{}", hoscode, hosScheduleId);
        }
    }

    // 根据医院编号和科室编号，查询排班规则数据（聚合统计）
    @Override
    public Map<String, Object> getRuleSchedule(ScheduleQueryDTO dto) {
        String hoscode = dto.getHoscode();
        String depcode = dto.getDepcode();
        long page = dto.getPage();
        long limit = dto.getSize();
        // 使用SQL GROUP BY聚合查询
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList =
                scheduleMapper.selectGroupByWorkDate(hoscode, depcode);

        // 总记录数
        int total = bookingScheduleRuleVoList.size();

        // Java内存分页
        List<BookingScheduleRuleVo> pagedList = bookingScheduleRuleVoList.stream()
                .skip((page - 1) * limit)
                .limit(limit)
                .collect(Collectors.toList());

        // 日期对应星期设置
        for (BookingScheduleRuleVo bookingScheduleRuleVo : pagedList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        // 设置返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList", pagedList);
        result.put("total", total);

        // 获取医院名称
        String hosName = hospitalService.getHospName(hoscode);
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hosName);
        result.put("baseMap", baseMap);
        return result;
    }

    // 根据医院编号、科室编号和工作日期查询排班详细信息（使用Redis缓存）
    @Override
    @Cacheable(value = "schedule", key = "#hoscode + ':' + #depcode + ':' + #workDate")
    public List<ScheduleVo> getDetailSchedule(String hoscode, String depcode, String workDate) {
        List<Schedule> scheduleList =
                scheduleMapper.selectByHoscodeAndDepcodeAndWorkDate(hoscode, depcode,
                        new DateTime(workDate).toDate());
        if (scheduleList.isEmpty()) {
            return Collections.emptyList();
        }
        // 预取公共维度：同一批排班同属一家医院、一个科室，
        // 只需查询一次，避免每条记录各查一次的 N+1 问题
        String hosname = hospitalService.getHospName(hoscode);
        String depname = departmentService.getDepName(hoscode, depcode);
        List<ScheduleVo> voList = new ArrayList<>(scheduleList.size());
        for (Schedule schedule : scheduleList) {
            voList.add(toScheduleVo(packageSchedule(schedule, hosname, depname)));
        }
        return voList;
    }

    // 获取可预约排班数据
    @Override
    public Object getBookingScheduleRule(ScheduleQueryDTO dto) {
        String hoscode = dto.getHoscode();
        String depcode = dto.getDepcode();
        Integer page = dto.getPage().intValue();
        Integer limit = dto.getSize().intValue();
        Map<String, Object> result = new HashMap<>();

        // 获取预约规则
        Hospital hospital = hospitalMapper.selectByHoscode(hoscode);
        if (null == hospital) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        // 获取可预约日期分页数据
        IPage iPage = this.getListDate(page, limit, bookingRule);
        // 当前页可预约日期
        List<Date> dateList = iPage.getRecords();

        // 使用SQL GROUP BY按日期列表分组统计
        List<BookingScheduleRuleVo> scheduleVoList =
                scheduleMapper.selectGroupByWorkDateIn(hoscode, depcode, dateList);

        // 合并数据：将统计数据按日期合并
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if (scheduleVoList != null && !scheduleVoList.isEmpty()) {
            scheduleVoMap = scheduleVoList.stream()
                    .collect(Collectors.toMap(
                            BookingScheduleRuleVo::getWorkDate,
                            vo -> vo,
                            (existing, replacement) -> existing));
        }

        // 组装可预约排班规则列表
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0, len = dateList.size(); i < len; i++) {
            Date date = dateList.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if (null == bookingScheduleRuleVo) {
                // 当天没有排班医生
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                bookingScheduleRuleVo.setDocCount(0);
                bookingScheduleRuleVo.setAvailableNumber(-1); // -1表示无号
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            // 计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            // 最后一页最后一条记录为即将预约  状态：0正常 1即将放号 -1当天已停止挂号
            if (i == len - 1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            // 当天预约如果过了停号时间，不能预约
            if (i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) {
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        // 可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());

        // 其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        // 医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        // 科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        // 大科室名称
        baseMap.put("bigname", department.getBigname());
        // 科室名称
        baseMap.put("depname", department.getDepname());
        // 月份
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        // 放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        // 停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    // 根据排班id获取排班数据（使用Redis缓存）
    @Override
    @Cacheable(value = "schedule", key = "#scheduleId")
    public ScheduleVo getScheduleId(String scheduleId) {
        // String id 转 Long id
        Schedule schedule = baseMapper.selectById(Long.parseLong(scheduleId));
        if (schedule == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        return toScheduleVo(this.packageSchedule(schedule));
    }

    // 根据排班id获取预约下单数据
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        // 排班信息
        Schedule schedule = baseMapper.selectById(Long.parseLong(scheduleId));
        if (schedule == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        schedule = this.packageSchedule(schedule);

        // 获取预约规则信息
        Hospital hospital = hospitalMapper.selectByHoscode(schedule.getHoscode());
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if (bookingRule == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        // 设置scheduleOrderVo数据
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        // 退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(
                new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(),
                bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        // 预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        // 预约截止时间
        DateTime endTime = this.getDateTime(
                new DateTime().plusDays(bookingRule.getCycle()).toDate(),
                bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        // 当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());
        return scheduleOrderVo;
    }

    // 获取可预约日期分页数据
    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        // 当天放号时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        // 预约周期
        int cycle = bookingRule.getCycle();
        // 如果当天放号时间已过，则预约周期后一天为即将放号时间，周期加1
        if (releaseTime.isBeforeNow()) {
            cycle += 1;
        }
        // 可预约所有日期
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        // 日期分页
        List<Date> pageDateList = new ArrayList<>();
        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;
        if (end > dateList.size()) {
            end = dateList.size();
        }
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        IPage<Date> iPage = new Page<>(page, 7, dateList.size());
        iPage.setRecords(pageDateList);
        return iPage;
    }

    /**
     * 将Date日期转换为DateTime（保留日期部分 + 拼接时间字符串）
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
    }

    // 排班PO转VO（处理workDate Date→String转换）
    private ScheduleVo toScheduleVo(Schedule schedule) {
        if (schedule == null) return null;
        ScheduleVo vo = BeanCopyUtils.copy(schedule, ScheduleVo.class);
        if (schedule.getWorkDate() != null) {
            vo.setWorkDate(new DateTime(schedule.getWorkDate()).toString("yyyy-MM-dd"));
        }
        return vo;
    }

    // 封装排班详情——设置医院名称、科室名称、日期对应星期
    private Schedule packageSchedule(Schedule schedule) {
        // 设置医院名称
        schedule.getParam().put("hosname", hospitalService.getHospName(schedule.getHoscode()));
        // 设置科室名称
        schedule.getParam().put("depname",
                departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        // 设置日期对应星期
        schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        return schedule;
    }

    /**
     * 封装排班详情（批量场景重载）
     *
     * <p>医院名称与科室名称由调用方预先查好传入。
     * 单条场景下每条记录都去查一次医院名+科室名没有明显问题，
     * 但列表查询会退化成 1 + 2N 次查询，因此批量入口统一走这个重载。
     */
    private Schedule packageSchedule(Schedule schedule, String hosname, String depname) {
        schedule.getParam().put("hosname", hosname);
        schedule.getParam().put("depname", depname);
        schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        return schedule;
    }

    /**
     * 根据日期获取周几（中文显示）
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
                break;
            default:
                break;
        }
        return dayOfWeek;
    }

    /**
     * 更新排班可预约数量（供 service_order 内部调用）
     *
     * <p>号源扣减使用<b>数据库层原子 UPDATE</b> 作为最后一道兜底：
     * <pre>
     *   UPDATE schedule SET available_number = available_number - 1
     *   WHERE hos_schedule_id = ? AND available_number &gt;= 1
     * </pre>
     * 由数据库行锁保证原子性。原实现是「先 SELECT 读出旧值 → 内存加减 → updateById 写回」，
     * 属于典型的 read-modify-write，并发下两个事务会读到同一个旧值，最终只扣减一次却卖出两个号（超卖）。
     *
     * <p>相比加 {@code @Version} 乐观锁字段，行级条件更新无需改表，也不需要额外的重试逻辑。
     *
     * @param hosScheduleId 医院侧排班编号
     * @param delta         变动量，负数表示扣减，正数表示回退
     */
    @Override
    public void updateAvailableNumber(String hosScheduleId, Integer delta) {
        if (delta == null || delta == 0) {
            return;
        }
        LambdaUpdateWrapper<Schedule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Schedule::getHosScheduleId, hosScheduleId)
                .eq(Schedule::getIsDeleted, 0);
        if (delta < 0) {
            // 扣减：余量必须足够，由数据库条件保证不会被扣成负数（防止超卖）
            // setSql 中的数值来自 Integer 类型的入参，不存在 SQL 注入风险
            updateWrapper.ge(Schedule::getAvailableNumber, -delta)
                    .setSql("available_number = available_number - " + Math.abs(delta));
        } else {
            updateWrapper.setSql("available_number = available_number + " + delta);
        }
        int rows = baseMapper.update(null, updateWrapper);
        if (rows > 0) {
            log.info("排班号源更新成功，排班编号：{}，变动量：{}", hosScheduleId, delta);
        } else {
            log.warn("排班号源未发生变动（排班不存在或余量不足），排班编号：{}，变动量：{}", hosScheduleId, delta);
        }
    }
}
