package com.yygh.hosp.controller;

import com.yygh.common.result.Result;
import com.yygh.hosp.service.ScheduleService;
import com.yygh.model.hosp.Schedule;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/admin/hosp/Schedule")
@RequiredArgsConstructor
/**
 * 排班控制器
 * @author XXJ
 */
public class ScheduleController {

    private final ScheduleService scheduleService;

    //根据医院编号和科室编号,查询排班规则数据
    @Operation(summary ="查询排班规则数据")
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable long page,
                                  @PathVariable long limit,
                                  @PathVariable String hoscode,
                                  @PathVariable String depcode) {
        Map<String,Object> map
                = scheduleService.getRuleSchedule(page,limit,hoscode,depcode);
        return Result.ok(map);
    }

    //根据医院编号,科室编号和工作日期查询排班详细信息
    @Operation(summary = "查询排班详细信息")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail( @PathVariable String hoscode,
                                     @PathVariable String depcode,
                                     @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getDetailSchedule(hoscode,depcode,workDate);
        return Result.ok(list);
    }

}
