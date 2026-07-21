package com.yygh.hosp.service;

import com.yygh.dto.HospitalQueryDTO;
import com.yygh.dto.HospitalSaveDTO;
import com.yygh.model.hosp.Hospital;
import com.yygh.vo.hosp.HospitalVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 医院服务接口
 * @author XXJ
 */
public interface HospitalService {
    // 上传医院接口
    void save(HospitalSaveDTO hospitalSaveDTO);

    // 查询医院接口
    HospitalVo getByHoscode(String hoscode);

    // 医院列表（条件查询带分页）
    IPage<Hospital> selectHospPage(HospitalQueryDTO hospitalQueryDTO);

    // 更新医院上线状态
    void updateStatus(String id, Integer status);

    // 医院详情信息
    HospitalVo getHospById(String id);

    // 获取医院名称
    String getHospName(String hoscode);

    // 根据医院名称获取医院列表
    List<HospitalVo> findByHosname(String hosname);

    // 根据医院编号查询医院预约挂号详情
    HospitalVo item(String hoscode);
}
