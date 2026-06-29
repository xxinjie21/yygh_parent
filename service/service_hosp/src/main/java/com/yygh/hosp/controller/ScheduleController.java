package com.yygh.hosp.controller;

import com.yygh.common.result.Result;
import com.yygh.dto.ScheduleQueryDTO;
import com.yygh.hosp.service.ScheduleService;
import com.yygh.vo.hosp.ScheduleVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("getScheduleRule")
    public Result getScheduleRule(@RequestBody ScheduleQueryDTO dto) {
        Map<String,Object> map
                = scheduleService.getRuleSchedule(dto);
        return Result.ok(map);
    }

    //根据医院编号,科室编号和工作日期查询排班详细信息
    @Operation(summary = "查询排班详细信息")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail( @PathVariable String hoscode,
                                     @PathVariable String depcode,
                                     @PathVariable String workDate) {
        List<ScheduleVo> list = scheduleService.getDetailSchedule(hoscode,depcode,workDate);
        return Result.ok(list);
    }

}
