package com.yygh.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yygh.enums.OrderStatusEnum;
import com.yygh.model.order.OrderInfo;
import com.yygh.order.mapper.OrderMapper;
import com.yygh.order.service.WeixinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 支付对账与订单超时补偿定时任务
 *
 * @author XXJ
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PaymentReconciliationTask {

    private final OrderMapper orderMapper;
    private final WeixinService weixinService;

    /**
     * 每10分钟检查超时未支付订单
     * 超过30分钟仍为UNPAID状态 → 自动取消并回退号源
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void cancelExpiredOrders() {
        log.info("开始执行超时订单取消任务");
        try {
            // 查询30分钟前创建且仍未支付的订单
            long thirtyMinutesAgo = System.currentTimeMillis() - 30 * 60 * 1000;
            LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderInfo::getOrderStatus, OrderStatusEnum.UNPAID.getStatus());
            wrapper.lt(OrderInfo::getCreateTime, new java.util.Date(thirtyMinutesAgo));

            List<OrderInfo> expiredOrders = orderMapper.selectList(wrapper);
            if (expiredOrders.isEmpty()) {
                log.info("无超时未支付订单");
                return;
            }

            log.info("发现 {} 个超时未支付订单，开始自动取消", expiredOrders.size());
            for (OrderInfo order : expiredOrders) {
                try {
                    // 调用微信查询确认订单状态
                    weixinService.queryPayStatus(order.getId());
                    // 仍然未支付则取消订单
                    weixinService.refund(order.getId());
                    log.info("超时订单已自动取消：{}", order.getOutTradeNo());
                } catch (Exception e) {
                    log.error("超时订单取消失败：{}", order.getOutTradeNo(), e);
                }
            }
        } catch (Exception e) {
            log.error("超时订单取消任务执行异常", e);
        }
    }

    /**
     * 每天凌晨2点执行支付对账
     * 对比微信侧支付记录与本地数据库，发现差异记录日志
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyReconciliation() {
        log.info("开始执行每日支付对账任务");
        try {
            // 查询昨日已支付的订单
            LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderInfo::getOrderStatus, OrderStatusEnum.PAID.getStatus());

            List<OrderInfo> paidOrders = orderMapper.selectList(wrapper);
            int mismatchCount = 0;
            for (OrderInfo order : paidOrders) {
                try {
                    // 调用微信查询接口确认订单实际支付状态
                    weixinService.queryPayStatus(order.getId());
                } catch (Exception e) {
                    mismatchCount++;
                    log.error("对账差异：订单 {} 支付状态不一致", order.getOutTradeNo(), e);
                }
            }
            log.info("每日对账完成，总订单：{}，差异：{}", paidOrders.size(), mismatchCount);
        } catch (Exception e) {
            log.error("每日对账任务执行异常", e);
        }
    }
}
