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

    /**
     * 创建挂号订单
     *
     * @param scheduleId 排班id
     * @param patientId  就诊人id
     * @param userId     当前登录用户id（校验就诊人归属，防止越权使用他人就诊人挂号）
     */
    Long saveOrder(String scheduleId, Long patientId, Long userId);

    /**
     * 查询订单详情
     *
     * @param orderId 订单id
     * @param userId  当前登录用户id（校验订单归属，防止横向越权查看他人订单）
     */
    OrderInfoVo getOrder(String orderId, Long userId);

    /**
     * 订单列表
     *
     * <p>调用方需保证 orderQueryDTO.userId 已设置为当前登录用户，
     * 本方法按该 userId 做行级过滤，只返回本人订单。
     */
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryDTO orderQueryDTO);

    /**
     * 取消预约（带归属校验，供Controller调用）
     *
     * @param orderId 订单id
     * @param userId  当前登录用户id
     */
    Boolean cancelOrder(Long orderId, Long userId);

    /**
     * 取消预约（<b>无归属校验，仅限系统内部调用</b>，如超时未支付自动取消的定时任务）
     */
    Boolean cancelOrder(Long orderId);

    //订单统计
    OrderCountVo getCountMap(OrderCountQueryVo orderCountQueryVo);
    //MQ回调：同步订单状态
    void updateOrderStatus(Long hosRecordId, Integer orderStatus);
}
