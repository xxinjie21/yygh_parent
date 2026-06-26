package com.yygh.order.service;

import com.yygh.model.order.OrderInfo;
import com.yygh.vo.order.OrderCountQueryVo;
import com.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 订单服务接口
 * @author XXJ
 */
public interface OrderService extends IService<OrderInfo> {
    //创建挂号订单
    Long saveOrder(String scheduleId, Long patientId);
    //根据订单id查询订单详情
    OrderInfo getOrder(String orderId);
    //订单列表（条件查询带分页）
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);
    //取消预约
    Boolean cancelOrder(Long orderId);
    //订单统计
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
    //MQ回调：同步订单状态
    void updateOrderStatus(Long hosRecordId, Integer orderStatus);
}
