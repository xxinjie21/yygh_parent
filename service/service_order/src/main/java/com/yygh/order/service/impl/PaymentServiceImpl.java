package com.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yygh.common.helper.HttpRequestHelper;
import com.yygh.enums.OrderStatusEnum;
import com.yygh.enums.PaymentStatusEnum;
import com.yygh.enums.PaymentTypeEnum;
import com.yygh.hosp.client.HospitalFeignClient;
import com.yygh.model.order.OrderInfo;
import com.yygh.model.order.PaymentInfo;
import com.yygh.order.mapper.PaymentInfoMapper;
import com.yygh.order.service.OrderService;
import com.yygh.order.service.PaymentService;
import com.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付服务实现类
 * @author XXJ
 */
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentService {

    private final OrderService orderService;

    private final HospitalFeignClient hospitalFeignClient;

    //向支付记录表添加信息
    @Override
    public void savePaymentInfo(OrderInfo order, Integer status) {
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOrderId, order.getId());
        queryWrapper.eq(PaymentInfo::getPaymentType, status);
        Long count = baseMapper.selectCount(queryWrapper);
        if(count >0) return;
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(status);
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+"|"
                +order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(order.getAmount());
        baseMapper.insert(paymentInfo);
    }

    //支付成功
    @Override
    public void paySuccess(String out_trade_no, Map<String, String> resultMap) {
        //1 根据订单编号得到支付记录
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOutTradeNo, out_trade_no);
        queryWrapper.eq(PaymentInfo::getPaymentType, PaymentTypeEnum.WEIXIN.getStatus());
        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);
        //2 更新支付记录信息
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));
        paymentInfo.setCallbackContent(resultMap.toString());
        baseMapper.updateById(paymentInfo);

        //3 根据订单号得到订单信息
        //4 更新订单信息
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);

        //5 调用医院接口，更新订单支付信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        Map<String,Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
//        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        String sign = signInfoVo.getSignKey();
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");
    }

    /**
     * 支付成功（APIv3回调，直接传transactionId）
     * 与 paySuccess(String, Map) 逻辑一致，适配APIv3回调格式
     */
    @Override
    public void paySuccessV3(String outTradeNo, String transactionId) {
        // 1 根据订单编号得到支付记录
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOutTradeNo, outTradeNo);
        queryWrapper.eq(PaymentInfo::getPaymentType, PaymentTypeEnum.WEIXIN.getStatus());
        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);
        // 幂等：已支付则跳过
        if (PaymentStatusEnum.PAID.getStatus().equals(paymentInfo.getPaymentStatus())) {
            return;
        }
        // 2 更新支付记录信息
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setTradeNo(transactionId);
        paymentInfo.setCallbackContent("APIv3回调: transactionId=" + transactionId);
        baseMapper.updateById(paymentInfo);
        // 3 更新订单信息
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);
        // 4 调用医院接口，更新订单支付信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = signInfoVo.getSignKey();
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");
    }

    //获取支付记录
    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOrderId, orderId);
        queryWrapper.eq(PaymentInfo::getPaymentType, paymentType);
        return baseMapper.selectOne(queryWrapper);
    }
}
