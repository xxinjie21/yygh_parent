package com.yygh.sta.controller;

import com.yygh.common.result.Result;
import com.yygh.order.client.OrderFeignClient;
import com.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
/**
 * 统计控制器
 * @author XXJ
 */
public class StatisticsController {
    private final OrderFeignClient orderFeignClient;

    //订单统计
    @GetMapping("getCountMap")
    public Result getCountMap(OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> countMap = orderFeignClient.getCountMap(orderCountQueryVo);
        return Result.ok(countMap);
    }
}
