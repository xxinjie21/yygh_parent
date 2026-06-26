package com.yygh.hosp.client;

import com.yygh.vo.hosp.ScheduleOrderVo;
import com.yygh.vo.order.SignInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 医院服务Feign远程调用客户端
 *
 * @author XXJ
 */
@FeignClient(value = "service-hosp", fallbackFactory = HospitalFeignClientFallbackFactory.class)
@Repository
public interface HospitalFeignClient {
    /**
     * 根据排班id获取预约下单数据
     */
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    /**
     * 获取医院签名信息
     */
    @GetMapping("/api/hosp/hospital/inner/getSignInfoVo/{hoscode}")
    SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);

    /**
     * 更新排班可预约数量（号源扣减/回退）
     */
    @PutMapping("/api/hosp/hospital/inner/updateAvailableNumber/{hosScheduleId}/{delta}")
    void updateAvailableNumber(@PathVariable("hosScheduleId") String hosScheduleId,
                               @PathVariable("delta") Integer delta);
}
