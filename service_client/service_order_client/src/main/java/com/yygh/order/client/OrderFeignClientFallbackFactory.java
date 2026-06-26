package com.yygh.order.client;

import com.yygh.vo.order.OrderCountQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单服务Feign客户端熔断降级工厂
 *
 * @author XXJ
 */
@Slf4j
@Component
public class OrderFeignClientFallbackFactory implements FallbackFactory<OrderFeignClient> {

    @Override
    public OrderFeignClient create(Throwable cause) {
        log.error("OrderFeignClient 调用失败，触发熔断降级", cause);
        return new OrderFeignClient() {
            @Override
            public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
                return new HashMap<>();
            }
        };
    }
}
