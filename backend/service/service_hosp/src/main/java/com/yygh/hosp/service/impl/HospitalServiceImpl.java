package com.yygh.hosp.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.yygh.cmn.client.DictFeignClient;
import com.yygh.common.cache.CacheBreakdownUtil;
import com.yygh.common.utils.BeanCopyUtils;
import com.yygh.dto.HospitalQueryDTO;
import com.yygh.dto.HospitalSaveDTO;
import com.yygh.hosp.mapper.HospitalMapper;
import com.yygh.hosp.service.HospitalService;
import com.yygh.model.hosp.Hospital;
import com.yygh.vo.hosp.HospitalVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 医院服务实现类
 * @author XXJ
 */
@RequiredArgsConstructor
@Service
public class HospitalServiceImpl extends
        ServiceImpl<HospitalMapper, Hospital> implements HospitalService {
    /**
     * 显式声明 slf4j 日志对象，遮蔽父类 ServiceImpl 继承来的 ibatis Log。
     *
     * <p>为什么不能直接依赖 @Slf4j：MyBatis-Plus 的 ServiceImpl 中有一个
     * <code>protected final org.apache.ibatis.logging.Log log</code> 字段，
     * Lombok 发现父类已存在同名 log 时会跳过生成，导致此处的 log 是 ibatis 的 Log 实现，
     * 它没有 info(String, Object...) 这类占位符重载，使用占位符打日志会直接编译失败。
     */
    private static final Logger log = LoggerFactory.getLogger(HospitalServiceImpl.class);


    private final HospitalMapper hospitalMapper;

    private final DictFeignClient dictFeignClient;

    private final CacheBreakdownUtil cacheBreakdownUtil;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 医院详情缓存 key 前缀
     *
     * <p>必须与 {@link #getByHoscode(String)} 中 CacheBreakdownUtil 写入的 key 完全一致。
     * 此前这里使用 {@code @CacheEvict(value="hospital")} 删除，它实际删的是 Spring Cache 的
     * {@code hospital::xxx}（双冒号），而数据存在 {@code hospital:xxx}（单冒号），
     * 两套机制互不相干，导致医院信息更新后缓存最长要等 10 分钟逻辑过期才生效。
     */
    private static final String HOSPITAL_CACHE_KEY_PREFIX = "hospital:";

    // 上传医院信息
    @Override
    public void save(HospitalSaveDTO hospitalSaveDTO) {
        // 把DTO转换为Hospital对象
        String mapString = JSONObject.toJSONString(hospitalSaveDTO);
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);

        // 判断是否存在相同医院编号的数据
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist = hospitalMapper.selectByHoscode(hoscode);

        if (hospitalExist != null) {
            // 已存在则修改
            hospital.setStatus(hospitalExist.getStatus());
            hospital.setCreateTime(hospitalExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospital.setId(hospitalExist.getId());
            baseMapper.updateById(hospital);
            log.info("医院信息更新成功，医院编号：{}，名称：{}", hoscode, hospital.getHosname());
        } else {
            // 不存在则添加
            hospital.setStatus(0); // 默认未上线
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            baseMapper.insert(hospital);
            log.info("医院信息新增成功，医院编号：{}，名称：{}", hoscode, hospital.getHosname());
        }
        evictHospitalCache(hoscode);
    }

    /**
     * 删除医院详情缓存（逻辑过期方案的缓存需要显式删除才能快速生效）
     */
    private void evictHospitalCache(String hoscode) {
        if (hoscode != null && !hoscode.isEmpty()) {
            stringRedisTemplate.delete(HOSPITAL_CACHE_KEY_PREFIX + hoscode);
        }
    }

    // 根据医院编号查询医院（逻辑过期 + 互斥锁防缓存击穿）
    @Override
    public HospitalVo getByHoscode(String hoscode) {
        return cacheBreakdownUtil.getWithLogicalExpire(
                "hospital:" + hoscode,
                600,
                () -> {
                    Hospital hospital = hospitalMapper.selectByHoscode(hoscode);
                    return toHospitalVo(hospital);
                },
                HospitalVo.class
        );
    }

    // 医院列表（条件查询 + 分页）
    @Override
    public IPage<Hospital> selectHospPage(HospitalQueryDTO dto) {
        // 构建分页对象
        Page<Hospital> pageParam = new Page<>(dto.getPage(), dto.getSize());
        // 构建查询条件
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hospital::getIsDeleted, 0);
        if (dto != null) {
            if (!StringUtils.isEmpty(dto.getHosname())) {
                wrapper.like(Hospital::getHosname, dto.getHosname());
            }
            if (!StringUtils.isEmpty(dto.getHoscode())) {
                wrapper.eq(Hospital::getHoscode, dto.getHoscode());
            }
            if (!StringUtils.isEmpty(dto.getHostype())) {
                wrapper.eq(Hospital::getHostype, dto.getHostype());
            }
            if (!StringUtils.isEmpty(dto.getProvinceCode())) {
                wrapper.eq(Hospital::getProvinceCode, dto.getProvinceCode());
            }
            if (!StringUtils.isEmpty(dto.getCityCode())) {
                wrapper.eq(Hospital::getCityCode, dto.getCityCode());
            }
            if (!StringUtils.isEmpty(dto.getDistrictCode())) {
                wrapper.eq(Hospital::getDistrictCode, dto.getDistrictCode());
            }
            if (dto.getStatus() != null) {
                wrapper.eq(Hospital::getStatus, dto.getStatus());
            }
        }
        IPage<Hospital> pages = baseMapper.selectPage(pageParam, wrapper);
        // 遍历设置医院等级名称
        pages.getRecords().forEach(this::setHospitalHosType);
        return pages;
    }

    // 更新医院上线状态
    @Override
    public void updateStatus(String id, Integer status) {
        // String id 转 Long id
        Hospital hospital = baseMapper.selectById(Long.parseLong(id));
        if (hospital == null) {
            log.warn("医院不存在，id：{}", id);
            return;
        }
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        baseMapper.updateById(hospital);
        // 上下线状态直接影响前台展示，需同步失效缓存
        evictHospitalCache(hospital.getHoscode());
        log.info("医院状态更新成功，id：{}，新状态：{}", id, status);
    }

    // 医院详情信息
    @Override
    public HospitalVo getHospById(String id) {
        Hospital hospital = this.setHospitalHosType(baseMapper.selectById(Long.parseLong(id)));
        if (hospital == null) {
            log.warn("医院不存在，id：{}", id);
            return null;
        }
        return toHospitalVo(hospital);
    }

    // 获取医院名称
    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalMapper.selectByHoscode(hoscode);
        if (null != hospital) {
            return hospital.getHosname();
        }
        return "";
    }

    // 根据医院名称模糊查询医院列表
    @Override
    public List<HospitalVo> findByHosname(String hosname) {
        List<Hospital> list = hospitalMapper.selectByHosnameLike(hosname);
        return BeanCopyUtils.copyList(list, HospitalVo.class);
    }

    // 根据医院编号查询医院预约挂号详情
    @Override
    public HospitalVo item(String hoscode) {
        Hospital hospital = this.setHospitalHosType(hospitalMapper.selectByHoscode(hoscode));
        return toHospitalVo(hospital);
    }

    // 医院PO转VO（处理bookingRule类型差异）
    private HospitalVo toHospitalVo(Hospital hospital) {
        if (hospital == null) return null;
        HospitalVo vo = BeanCopyUtils.copy(hospital, HospitalVo.class);
        // bookingRule字段Hospital.getBookingRule()返回BookingRule对象，BeanUtils类型不匹配跳过
        if (hospital.getBookingRule() != null) {
            vo.setBookingRule(JSONObject.toJSONString(hospital.getBookingRule()));
        }
        return vo;
    }

    // 设置医院等级、省市区名称
    private Hospital setHospitalHosType(Hospital hospital) {
        if (hospital == null) {
            return null;
        }
        // 获取医院等级名称
        String hostypeString = dictFeignClient.getName("Hostype", hospital.getHostype());
        // 查询省、市、地区名称
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());
        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("fullAddress",
                provinceString + cityString + districtString + hospital.getAddress());
        return hospital;
    }
}
