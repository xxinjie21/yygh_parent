package com.yygh.user.client;

import com.yygh.model.user.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 用户服务Feign客户端熔断降级工厂
 *
 * @author XXJ
 */
@Slf4j
@Component
public class PatientFeignClientFallbackFactory implements FallbackFactory<PatientFeignClient> {

    @Override
    public PatientFeignClient create(Throwable cause) {
        log.error("PatientFeignClient 调用失败，触发熔断降级", cause);
        return new PatientFeignClient() {
            @Override
            public Patient getPatient(Long id) {
                return null;
            }
        };
    }
}
