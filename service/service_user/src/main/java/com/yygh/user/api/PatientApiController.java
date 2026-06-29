package com.yygh.user.api;

import com.yygh.common.result.Result;
import com.yygh.common.utils.AuthContextHolder;
import com.yygh.common.utils.BeanCopyUtils;
import com.yygh.model.user.Patient;
import com.yygh.user.service.PatientService;
import com.yygh.vo.user.PatientVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 就诊人管理API控制器
 * @author XXJ
 */
//就诊人管理接口
@RestController
@RequestMapping("/api/user/patient")
@RequiredArgsConstructor
public class PatientApiController {
    private final PatientService patientService;
    //获取就诊人列表
    @GetMapping("auth/findAll")
    public Result findAll(@RequestHeader("token") String token) {
        Long userId = AuthContextHolder.getUserId(token);
        List<PatientVo> list = patientService.findAllUserId(userId);
        return Result.ok(list);
    }
    //添加就诊人
    @PostMapping("auth/save")
    public Result savePatient(@RequestBody Patient patient, @RequestHeader("token") String token) {
        Long userId = AuthContextHolder.getUserId(token);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }
    //根据id获取就诊人信息
    @GetMapping("auth/get/{id}")
    public Result getPatient(@PathVariable Long id) {
        PatientVo patientVo = patientService.getPatientId(id);
        return Result.ok(patientVo);
    }
    //修改就诊人
    @PostMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return Result.ok();
    }
    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public Result removePatient(@PathVariable Long id) {
        patientService.removeById(id);
        return Result.ok();
    }

    //获取就诊人
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(@PathVariable("id") Long id) {
        Patient patient= patientService.getById(id);
        return patient;
    }
}
