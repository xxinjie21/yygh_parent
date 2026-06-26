package com.yygh.order.service;

import com.yygh.model.order.PaymentInfo;
import com.yygh.model.order.RefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

/**
 * 退款信息服务接口
 * @author XXJ
 */
public interface RefundInfoService extends IService<RefundInfo> {
   //保存退款记录
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}
