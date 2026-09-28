package com.yygh.order.api;

import com.yygh.common.result.Result;
import com.yygh.common.utils.AuthContextHolder;
import com.yygh.enums.OrderStatusEnum;
import com.yygh.model.order.OrderInfo;
import com.yygh.order.service.OrderService;
import com.yygh.vo.order.OrderCountQueryVo;
import com.yygh.vo.order.OrderCountVo;
import com.yygh.vo.order.OrderInfoVo;
import com.yygh.dto.OrderQueryDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单接口")
@RestController
@RequestMapping("/api/order/orderInfo")
@RequiredArgsConstructor
/**
 * 订单API控制器
 * @author XXJ
 */
public class OrderApiController {
    private final OrderService orderService;


    //创建挂号订单（校验就诊人归属当前登录用户，防止越权替他人挂号）
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result submitOrder(@PathVariable String scheduleId, @PathVariable Long patientId,
                              @RequestHeader("token") String token) {
        Long userId = AuthContextHolder.getUserId(token);
        Long orderId = orderService.saveOrder(scheduleId, patientId, userId);
        return Result.ok(orderId);
    }

    //根据订单id查询订单详情（校验订单归属，防止遍历orderId查看他人订单）
    @GetMapping("auth/getOrders/{orderId}")
    public Result<OrderInfoVo> getOrders(@PathVariable String orderId,
                                         @RequestHeader("token") String token) {
        Long userId = AuthContextHolder.getUserId(token);
        OrderInfoVo orderInfoVo = orderService.getOrder(orderId, userId);
        return Result.ok(orderInfoVo);
    }

    //订单列表（条件查询带分页）
    @PostMapping("auth/list")
    public Result list(@RequestBody OrderQueryDTO dto, @RequestHeader("token") String token) {
        dto.setUserId(AuthContextHolder.getUserId(token));
        Page<OrderInfo> pageParam = new Page<>(dto.getPage(), dto.getSize());
        IPage<OrderInfo> pageModel =
                orderService.selectPage(pageParam, dto);
        return Result.ok(pageModel);
    }

    //获取订单状态
    @GetMapping("auth/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    //取消预约（校验订单归属，防止越权取消他人订单）
    @GetMapping("auth/cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId, @RequestHeader("token") String token) {
        Long userId = AuthContextHolder.getUserId(token);
        Boolean isOrder = orderService.cancelOrder(orderId, userId);
        return Result.ok(isOrder);
    }

    //订单统计
    @PostMapping("inner/getCountMap")
    public OrderCountVo getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        return orderService.getCountMap(orderCountQueryVo);
    }
}
