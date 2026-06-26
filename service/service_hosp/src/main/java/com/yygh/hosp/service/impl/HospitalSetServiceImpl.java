package com.yygh.hosp.service.impl;

import com.yygh.common.exception.YyghException;
import com.yygh.common.result.ResultCodeEnum;
import com.yygh.hosp.mapper.HospitalSetMapper;
import com.yygh.hosp.service.HospitalSetService;
import com.yygh.model.hosp.HospitalSet;
import com.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 医院设置服务实现类
 * @author XXJ
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {
    //2 根据传递过来医院编码,查询数据库,查询签名
    @Override
    public String getSignKey(String hoscode) {
        LambdaQueryWrapper<HospitalSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HospitalSet::getHoscode, hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        return hospitalSet.getSignKey();
    }

    //获取医院签名信息
    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        LambdaQueryWrapper<HospitalSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HospitalSet::getHoscode, hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if (hospitalSet == null) {
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;
    }
}
