package com.yygh.hosp.controller.api;

import com.yygh.common.result.Result;
import com.yygh.hosp.service.DepartmentService;
import com.yygh.hosp.service.HospitalService;
import com.yygh.hosp.service.HospitalSetService;
import com.yygh.hosp.service.ScheduleService;
import com.yygh.model.hosp.Hospital;
import com.yygh.model.hosp.Schedule;
import com.yygh.vo.hosp.DepartmentVo;
import com.yygh.vo.hosp.HospitalQueryVo;
import com.yygh.vo.hosp.ScheduleOrderVo;
import com.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(tags = "医院管理接口")
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
    @ApiOperation(value = "获取分页列表")
    @GetMapping("/findHospList/{page}/{limit}")
    public Result index(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo) {
        //显示上线的医院
        hospitalQueryVo.setStatus(1);
        IPage<Hospital> pageModel = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        List<Hospital> content = pageModel.getRecords();
        long totalPages = pageModel.getPages();
        return Result.ok(pageModel);
    }

    //根据医院名称获取医院列表
    @ApiOperation(value = "根据医院名称获取医院列表")
    @GetMapping("/findByHosname/{hosname}")
    public Result findByHosname(@PathVariable String hosname) {
        List<Hospital> list = hospitalService.findByHosname(hosname);
        return Result.ok(list);
    }

    //根据医院编号获取科室列表
    @ApiOperation(value = "根据医院编号获取科室列表")
    @GetMapping("/department/{hoscode}")
    public Result index(@PathVariable String hoscode) {
        List<DepartmentVo> deptTree = departmentService.findDeptTree(hoscode);
        return Result.ok(deptTree);
    }

    //根据医院编号查询医院预约挂号详情
    @ApiOperation(value = "根据医院编号查询医院预约挂号详情")
    @GetMapping("/findHospDetail/{hoscode}")
    public Result item(@PathVariable String hoscode) {
        Map<String, Object> map = hospitalService.item(hoscode);
        return Result.ok(map);
    }

    //获取可预约排班数据
    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(@PathVariable Integer page, @PathVariable Integer limit,
                                     @PathVariable String hoscode, @PathVariable String depcode) {
        return Result.ok(scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode));
    }

    //获取排班数据
    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(@PathVariable String hoscode, @PathVariable String depcode, @PathVariable String workDate) {
        return Result.ok(scheduleService.getDetailSchedule(hoscode, depcode, workDate));
    }

    //根据排班id获取排班数据
    @ApiOperation(value = "根据排班id获取排班数据")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(@PathVariable String scheduleId) {
        Schedule schedule = scheduleService.getScheduleId(scheduleId);
        return Result.ok(schedule);
    }

    //根据排班id获取预约下单数据
    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

    //获取医院签名信息
    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode) {
        return hospitalSetService.getSignInfoVo(hoscode);
    }

    //更新排班可预约数量（内部调用）
    @ApiOperation(value = "更新排班可预约数量")
    @PutMapping("inner/updateAvailableNumber/{hosScheduleId}/{delta}")
    public void updateAvailableNumber(@PathVariable String hosScheduleId, @PathVariable Integer delta) {
        scheduleService.updateAvailableNumber(hosScheduleId, delta);
    }
}

