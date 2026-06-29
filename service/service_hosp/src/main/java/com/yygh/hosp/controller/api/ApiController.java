package com.yygh.hosp.controller.api;


import com.yygh.common.exception.YyghException;
import com.yygh.common.helper.HttpRequestHelper;
import com.yygh.common.result.Result;
import com.yygh.common.result.ResultCodeEnum;
import com.yygh.hosp.service.DepartmentService;
import com.yygh.hosp.service.HospitalService;
import com.yygh.hosp.service.HospitalSetService;
import com.yygh.hosp.service.ScheduleService;
import com.yygh.model.hosp.Department;
import com.yygh.model.hosp.Hospital;
import com.yygh.model.hosp.Schedule;
import com.yygh.vo.hosp.DepartmentQueryVo;
import com.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result removeSchedule(@RequestBody Map<String, Object> paramMap){
        String hoscode = (String)paramMap.get("hoscode");
        String hosScheduleId = String.valueOf(paramMap.get("hosScheduleId"));
        if(StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        scheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();
    }
    //查询排班接口
    @Operation(summary = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result findSchedule(@RequestBody Map<String, Object> paramMap){
        //医院编号 科室编号
        String hoscode = (String)paramMap.get("hoscode");
        String depcode = (String)paramMap.get("depcode");
        //当前页和每页记录数
        int page = paramMap.get("page") == null ? 1 : ((Number) paramMap.get("page")).intValue();
        int limit = paramMap.get("limit") == null ? 10 : ((Number) paramMap.get("limit")).intValue();
        if(StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        IPage<Schedule> pageModel = scheduleService.findPageSchedule(page, limit, scheduleQueryVo);
        return Result.ok(pageModel);
    }
    //上传排班接口
    @Operation(summary = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(@RequestBody Map<String, Object> paramMap){
        //必须参数校验
        String hoscode = (String)paramMap.get("hoscode");
        if(StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        scheduleService.save(paramMap);
        return Result.ok();
    }

    //删除科室接口
    @Operation(summary = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(@RequestBody Map<String, Object> paramMap) {
        String hoscode = (String)paramMap.get("hoscode");
        String depcode = (String)paramMap.get("depcode");
        if(StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
//        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
//            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
//        }
        departmentService.remove(hoscode, depcode);
        return Result.ok();
    }
    //查询科室接口
    @Operation(summary = "获取分页列表")
    @PostMapping("department/list")
    public Result findDepartment(@RequestBody Map<String, Object> paramMap) {
        //医院编号
        String hoscode = (String)paramMap.get("hoscode");
        String depcode = (String)paramMap.get("depcode");
        //当前页和每页记录数
        int page = paramMap.get("page") == null ? 1 : ((Number) paramMap.get("page")).intValue();
        int limit = paramMap.get("limit") == null ? 10 : ((Number) paramMap.get("limit")).intValue();

        if(StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        departmentQueryVo.setDepcode(depcode);
        IPage<Department> pageModel = departmentService.findPageDepartment(page, limit, departmentQueryVo);
        return Result.ok(pageModel);
    }
    //上传科室接口
    @Operation(summary = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(@RequestBody Map<String, Object> paramMap){

        //获取医院编号
        String hoscode=(String)paramMap.get("hoscode");
        //签名校验
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        departmentService.save(paramMap);
        return Result.ok();
    }

    //查询医院
    @Operation(summary = "获取医院信息")
    @PostMapping("hospital/show")
    public Result getHospital(@RequestBody Map<String, Object> paramMap){
        //获取医院编号
        String hoscode=(String)paramMap.get("hoscode");
        //签名校验
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //调用service方法实现根据医院查询
        Hospital hospital= hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    //上传医院接口
    @Operation(summary = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHospital(@RequestBody Map<String, Object> paramMap) {
        String hoscode = (String) paramMap.get("hoscode");
        //签名校验
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //JSON传输无form-encoded的+转空格问题，无需replaceAll
        //调用service方法
        hospitalService.save(paramMap);
        return Result.ok();
    }
}
