package com.yygh.hosp.controller.api;

import com.yygh.common.result.Result;
import com.yygh.dto.HospitalQueryDTO;
import com.yygh.dto.ScheduleQueryDTO;
import com.yygh.hosp.service.DepartmentService;
import com.yygh.hosp.service.HospitalService;
import com.yygh.hosp.service.HospitalSetService;
import com.yygh.hosp.service.ScheduleService;
import com.yygh.vo.hosp.DepartmentVo;
import com.yygh.model.hosp.Hospital;
import com.yygh.vo.hosp.HospitalVo;
import com.yygh.vo.hosp.ScheduleOrderVo;
import com.yygh.vo.hosp.ScheduleVo;
import com.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "医院管理接口")
@RestController
@RequestMapping("/api/hosp/hospital")
@RequiredArgsConstructor
/**
 * 医院管理API控制器（前台门户调用）
 * @author XXJ
 */
public class HospitalApiController {
    private final HospitalService hospitalService;

    private final DepartmentService departmentService;

    private final ScheduleService scheduleService;

    private final HospitalSetService hospitalSetService;

    //医院列表
    @Operation(summary = "获取分页列表")
    @PostMapping("findHospList")
    public Result index(@RequestBody HospitalQueryDTO dto) {
        //显示上线的医院
        dto.setStatus(1);
        IPage<Hospital> pageModel = hospitalService.selectHospPage(dto);
        return Result.ok(pageModel);
    }

    //根据医院名称获取医院列表
    @Operation(summary = "根据医院名称获取医院列表")
    @GetMapping("/findByHosname/{hosname}")
    public Result findByHosname(@PathVariable String hosname) {
        List<HospitalVo> list = hospitalService.findByHosname(hosname);
        return Result.ok(list);
    }

    //根据医院编号获取科室列表
    @Operation(summary = "根据医院编号获取科室列表")
    @GetMapping("/department/{hoscode}")
    public Result index(@PathVariable String hoscode) {
        List<DepartmentVo> deptTree = departmentService.findDeptTree(hoscode);
        return Result.ok(deptTree);
    }

    //根据医院编号查询医院预约挂号详情
    @Operation(summary = "根据医院编号查询医院预约挂号详情")
    @GetMapping("/findHospDetail/{hoscode}")
    public Result item(@PathVariable String hoscode) {
        HospitalVo vo = hospitalService.item(hoscode);
        return Result.ok(vo);
    }

    //获取可预约排班数据
    @Operation(summary = "获取可预约排班数据")
    @PostMapping("auth/getBookingScheduleRule")
    public Result getBookingSchedule(@RequestBody ScheduleQueryDTO dto) {
        return Result.ok(scheduleService.getBookingScheduleRule(dto));
    }

    //获取排班数据
    @Operation(summary = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(@PathVariable String hoscode, @PathVariable String depcode, @PathVariable String workDate) {
        return Result.ok(scheduleService.getDetailSchedule(hoscode, depcode, workDate));
    }

    //根据排班id获取排班数据
    @Operation(summary = "根据排班id获取排班数据")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(@PathVariable String scheduleId) {
        ScheduleVo vo = scheduleService.getScheduleId(scheduleId);
        return Result.ok(vo);
    }

    //根据排班id获取预约下单数据
    @Operation(summary = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

    //获取医院签名信息
    @Operation(summary = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode) {
        return hospitalSetService.getSignInfoVo(hoscode);
    }

    //更新排班可预约数量（内部调用）
    @Operation(summary = "更新排班可预约数量")
    @PutMapping("inner/updateAvailableNumber/{hosScheduleId}/{delta}")
    public void updateAvailableNumber(@PathVariable String hosScheduleId, @PathVariable Integer delta) {
        scheduleService.updateAvailableNumber(hosScheduleId, delta);
    }
}

