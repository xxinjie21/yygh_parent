package com.yygh.hosp.client;

import com.yygh.vo.hosp.ScheduleOrderVo;
import com.yygh.vo.order.SignInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 医院服务Feign客户端熔断降级工厂
 *
 * @author XXJ
 */
@Slf4j
@Component
public class HospitalFeignClientFallbackFactory implements FallbackFactory<HospitalFeignClient> {

    @Override
    public HospitalFeignClient create(Throwable cause) {
        log.error("HospitalFeignClient 调用失败，触发熔断降级", cause);
        return new HospitalFeignClient() {
            @Override
            public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
                return null;
            }

            @Override
            public SignInfoVo getSignInfoVo(String hoscode) {
                return null;
            }

            @Override
            public void updateAvailableNumber(String hosScheduleId, Integer delta) {
                // 降级时不做号源扣减，避免数据不一致
            }
        };
    }
}
