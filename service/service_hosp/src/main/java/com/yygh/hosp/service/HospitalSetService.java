package com.yygh.hosp.service;

import com.yygh.model.hosp.HospitalSet;
import com.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 医院设置服务接口
 * @author XXJ
 */
public interface HospitalSetService extends IService<HospitalSet> {
    //根据传过来的医院编号 查询数据库 查询签名
    String getSignKey(String hoscode);
    //获取医院签名信息
    SignInfoVo getSignInfoVo(String hoscode);
}
