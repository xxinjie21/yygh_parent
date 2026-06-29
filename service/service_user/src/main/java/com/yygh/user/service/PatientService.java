package com.yygh.user.service;

import com.yygh.model.user.Patient;
import com.yygh.vo.user.PatientVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 就诊人服务接口
 * @author XXJ
 */
public interface PatientService extends IService<Patient> {
    List<PatientVo> findAllUserId(Long userId);
    PatientVo getPatientId(Long id);
}
