package com.yygh.hosp.controller.api;


import com.yygh.common.exception.YyghException;
import com.yygh.common.helper.HttpRequestHelper;
import com.yygh.common.result.Result;
import com.yygh.common.result.ResultCodeEnum;
import com.yygh.dto.DepartmentQueryDTO;
import com.yygh.dto.DepartmentRemoveDTO;
import com.yygh.dto.DepartmentSaveDTO;
import com.yygh.dto.HospitalSaveDTO;
import com.yygh.dto.HospitalShowDTO;
import com.yygh.dto.ScheduleQueryDTO;
import com.yygh.dto.ScheduleRemoveDTO;
import com.yygh.dto.ScheduleSaveDTO;
import com.yygh.hosp.service.DepartmentService;
import com.yygh.hosp.service.HospitalService;
import com.yygh.hosp.service.HospitalSetService;
import com.yygh.hosp.service.ScheduleService;
import com.yygh.model.hosp.Department;
import com.yygh.model.hosp.Schedule;
import com.yygh.vo.hosp.HospitalVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import java.util.Map;

@Tag(name = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
@RequiredArgsConstructor
/**
 * 医院管理API控制器（供医院系统调用）
 * @author XXJ
 */
public class ApiController {

    private final HospitalService hospitalService;

    private final HospitalSetService hospitalSetService;

    private final DepartmentService departmentService;

    private final ScheduleService scheduleService;

    //删除排班接口
    @Operation(summary = "删除排班")
    @PostMapping("schedule/remove")
    public Result removeSchedule(@RequestBody ScheduleRemoveDTO dto){
        if(StringUtils.isEmpty(dto.getHoscode())) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        String json = JSONObject.toJSONString(dto);
        Map<String, Object> paramMap = JSONObject.parseObject(json, Map.class);
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(dto.getHoscode()))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        scheduleService.remove(dto.getHoscode(), dto.getHosScheduleId());
        return Result.ok();
    }
    //查询排班接口
    @Operation(summary = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result findSchedule(@RequestBody ScheduleQueryDTO dto){
        if(StringUtils.isEmpty(dto.getHoscode())) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        String json = JSONObject.toJSONString(dto);
        Map<String, Object> paramMap = JSONObject.parseObject(json, Map.class);
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(dto.getHoscode()))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        IPage<Schedule> pageModel = scheduleService.findPageSchedule(dto);
        return Result.ok(pageModel);
    }
    //上传排班接口
    @Operation(summary = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(@RequestBody ScheduleSaveDTO dto){
        //必须参数校验
        String hoscode = dto.getHoscode();
        if(StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        String json = JSONObject.toJSONString(dto);
        Map<String, Object> paramMap = JSONObject.parseObject(json, Map.class);
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        scheduleService.save(dto);
        return Result.ok();
    }

    //删除科室接口
    @Operation(summary = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(@RequestBody DepartmentRemoveDTO dto) {
        String hoscode = dto.getHoscode();
        String depcode = dto.getDepcode();
        if(StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        String json = JSONObject.toJSONString(dto);
        Map<String, Object> paramMap = JSONObject.parseObject(json, Map.class);
//        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
//            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
//        }
        departmentService.remove(hoscode, depcode);
        return Result.ok();
    }
    //查询科室接口
    @Operation(summary = "获取分页列表")
    @PostMapping("department/list")
    public Result findDepartment(@RequestBody DepartmentQueryDTO dto) {
        if(StringUtils.isEmpty(dto.getHoscode())) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        String json = JSONObject.toJSONString(dto);
        Map<String, Object> paramMap = JSONObject.parseObject(json, Map.class);
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(dto.getHoscode()))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        IPage<Department> pageModel = departmentService.findPageDepartment(dto);
        return Result.ok(pageModel);
    }
    //上传科室接口
    @Operation(summary = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(@RequestBody DepartmentSaveDTO dto){

        //获取医院编号
        String hoscode = dto.getHoscode();
        //签名校验
        String json = JSONObject.toJSONString(dto);
        Map<String, Object> paramMap = JSONObject.parseObject(json, Map.class);
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        departmentService.save(dto);
        return Result.ok();
    }

    //查询医院
    @Operation(summary = "获取医院信息")
    @PostMapping("hospital/show")
    public Result getHospital(@RequestBody HospitalShowDTO dto){
        //获取医院编号
        String hoscode = dto.getHoscode();
        //签名校验
        String json = JSONObject.toJSONString(dto);
        Map<String, Object> paramMap = JSONObject.parseObject(json, Map.class);
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //调用service方法实现根据医院查询
        HospitalVo hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    //上传医院接口
    @Operation(summary = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHospital(@RequestBody HospitalSaveDTO dto) {
        String hoscode = dto.getHoscode();
        //签名校验
        String json = JSONObject.toJSONString(dto);
        Map<String, Object> paramMap = JSONObject.parseObject(json, Map.class);
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //JSON传输无form-encoded的+转空格问题，无需replaceAll
        //调用service方法
        hospitalService.save(dto);
        return Result.ok();
    }
}
