package com.yygh.hosp.service;

import com.yygh.model.hosp.Hospital;
import com.yygh.vo.hosp.HospitalQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * 医院服务接口
 * @author XXJ
 */
public interface HospitalService {
    // 上传医院接口
    void save(Map<String, Object> paramMap);

    // 查询医院接口
    Hospital getByHoscode(String hoscode);

    // 医院列表（条件查询带分页）
    IPage<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    // 更新医院上线状态
    void updateStatus(String id, Integer status);

    // 医院详情信息
    Map<String, Object> getHospById(String id);

    // 获取医院名称
    String getHospName(String hoscode);

    // 根据医院名称获取医院列表
    List<Hospital> findByHosname(String hosname);

    // 根据医院编号查询医院预约挂号详情
    Map<String, Object> item(String hoscode);
}
