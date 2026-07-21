package com.yygh.hospital.service;

import java.io.IOException;
import java.util.Map;

/**
 * 医院服务接口，定义预约下单、支付状态更新、取消预约等操作
 *
 * @author XXJ
 */
public interface HospitalService {

    /**
     * 预约下单
     * @param paramMap
     * @return
     */
    Map<String, Object> submitOrder(Map<String, Object> paramMap);

    /**
     * 更新支付状态
     * @param paramMap
     */
    void updatePayStatus(Map<String, Object> paramMap);

    /**
     * 更新取消预约状态
     * @param paramMap
     */
    void updateCancelStatus(Map<String, Object> paramMap);


}
