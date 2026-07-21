package com.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yygh.dto.DepartmentQueryDTO;
import com.yygh.dto.DepartmentSaveDTO;
import com.yygh.hosp.mapper.DepartmentMapper;
import com.yygh.hosp.service.DepartmentService;
import com.yygh.model.hosp.Department;
import com.yygh.vo.hosp.DepartmentVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 科室服务实现类
 * @author XXJ
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DepartmentServiceImpl extends
        ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    // 上传科室信息
    @Override
    public void save(DepartmentSaveDTO departmentSaveDTO) {
        String dtoString = JSONObject.toJSONString(departmentSaveDTO);
        Department department = JSONObject.parseObject(dtoString, Department.class);

        // 根据医院编号和科室编号查询是否已存在
        Department departmentExist = departmentMapper.selectByHoscodeAndDepcode(
                department.getHoscode(), department.getDepcode());
        if (departmentExist != null) {
            // 已存在则更新
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            BeanUtils.copyProperties(department, departmentExist, "id", "createTime");
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            baseMapper.updateById(departmentExist);
            log.info("科室信息更新成功，医院编号：{}，科室编号：{}", department.getHoscode(), department.getDepcode());
        } else {
            // 不存在则新增
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            baseMapper.insert(department);
            log.info("科室信息新增成功，医院编号：{}，科室编号：{}", department.getHoscode(), department.getDepcode());
        }
    }

    // 查询科室（分页 + 条件查询）
    @Override
    public IPage<Department> findPageDepartment(DepartmentQueryDTO dto) {
        Page<Department> pageParam = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getIsDeleted, 0);
        if (dto != null) {
            if (!StringUtils.isEmpty(dto.getHoscode())) {
                wrapper.eq(Department::getHoscode, dto.getHoscode());
            }
            if (!StringUtils.isEmpty(dto.getDepcode())) {
                wrapper.like(Department::getDepcode, dto.getDepcode());
            }
        }
        IPage<Department> pages = baseMapper.selectPage(pageParam, wrapper);
        return pages;
    }

    // 删除科室
    @Override
    @CacheEvict(value = "dept", key = "#hoscode + ':' + #depcode")
    public void remove(String hoscode, String depcode) {
        Department department = departmentMapper.selectByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            baseMapper.deleteById(department.getId());
            log.info("科室删除成功，医院编号：{}，科室编号：{}", hoscode, depcode);
        }
    }

    // 根据医院编号，查询医院所有科室列表（按大科室分组）
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        // 最终数据封装
        List<DepartmentVo> result = new ArrayList<>();

        // 查询该医院下所有科室
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getHoscode, hoscode);
        wrapper.eq(Department::getIsDeleted, 0);
        List<Department> departmentList = baseMapper.selectList(wrapper);

        // 按大科室编号分组
        Map<String, List<Department>> departmentMap = departmentList.stream()
                .collect(Collectors.groupingBy(Department::getBigcode));

        // 遍历map集合，组装树形结构
        for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            String bigcode = entry.getKey();
            List<Department> deptList = entry.getValue();

            // 封装大科室
            DepartmentVo departmentVo1 = new DepartmentVo();
            departmentVo1.setDepcode(bigcode);
            departmentVo1.setDepname(deptList.get(0).getBigname());

            // 封装小科室
            List<DepartmentVo> children = new ArrayList<>();
            for (Department department : deptList) {
                DepartmentVo departmentVo2 = new DepartmentVo();
                departmentVo2.setDepcode(department.getDepcode());
                departmentVo2.setDepname(department.getDepname());
                children.add(departmentVo2);
            }
            // 小科室list放入大科室children
            departmentVo1.setChildren(children);
            // 最终result
            result.add(departmentVo1);
        }
        return result;
    }

    // 根据科室编号和医院编号查询科室名称（使用Redis缓存）
    @Override
    @Cacheable(value = "dept", key = "#hoscode + ':' + #depcode")
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentMapper.selectByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            return department.getDepname();
        }
        return null;
    }

    // 查询科室（使用Redis缓存）
    @Override
    @Cacheable(value = "dept", key = "#hoscode + ':' + #depcode")
    public Department getDepartment(String hoscode, String depcode) {
        Department department = departmentMapper.selectByHoscodeAndDepcode(hoscode, depcode);
        return department;
    }
}
