package com.yygh.order.service.impl;

import com.yygh.enums.RefundStatusEnum;
import com.yygh.model.order.PaymentInfo;
import com.yygh.model.order.RefundInfo;
import com.yygh.order.mapper.RefundInfoMapper;
import com.yygh.order.service.RefundInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
/**
 * 退款信息服务实现类
 * @author XXJ
 */
@RequiredArgsConstructor
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    private final RefundInfoMapper refundInfoMapper;

    //保存退款记录
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        LambdaQueryWrapper<RefundInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RefundInfo::getOrderId, paymentInfo.getOrderId());
        queryWrapper.eq(RefundInfo::getPaymentType, paymentInfo.getPaymentType());
        RefundInfo refundInfo = refundInfoMapper.selectOne(queryWrapper);
        if(null != refundInfo) return refundInfo;
        // 保存交易记录
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(new Date());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo.setSubject(paymentInfo.getSubject());
        //paymentInfo.setSubject("test");
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        refundInfoMapper.insert(refundInfo);
        return refundInfo;
    }
}
