package com.yygh.user.client;

import com.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign远程调用客户端（就诊人）
 *
 * @author XXJ
 */
@FeignClient(value = "service-user", fallbackFactory = PatientFeignClientFallbackFactory.class)
@Repository
public interface PatientFeignClient {
    //获取就诊人
    @GetMapping("/api/user/patient/inner/get/{id}")
    Patient getPatient(@PathVariable("id") Long id);
}
