package com.yygh.order.service;

import com.yygh.dto.OrderQueryDTO;
import com.yygh.model.order.OrderInfo;
import com.yygh.vo.order.OrderCountQueryVo;
import com.yygh.vo.order.OrderCountVo;
import com.yygh.vo.order.OrderInfoVo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 订单服务接口
 * @author XXJ
 */
public interface OrderService extends IService<OrderInfo> {
    Long saveOrder(String scheduleId, Long patientId);
    OrderInfoVo getOrder(String orderId);
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryDTO orderQueryDTO);
    //取消预约
    Boolean cancelOrder(Long orderId);
    //订单统计
    OrderCountVo getCountMap(OrderCountQueryVo orderCountQueryVo);
    //MQ回调：同步订单状态
    void updateOrderStatus(Long hosRecordId, Integer orderStatus);
}
