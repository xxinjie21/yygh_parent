package com.yygh.hosp.service;

import com.yygh.dto.DepartmentQueryDTO;
import com.yygh.dto.DepartmentSaveDTO;
import com.yygh.model.hosp.Department;
import com.yygh.vo.hosp.DepartmentVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * 科室服务接口
 * @author XXJ
 */
public interface DepartmentService {
    void save(DepartmentSaveDTO departmentSaveDTO);

    IPage<Department> findPageDepartment(DepartmentQueryDTO departmentQueryDTO);

    void remove(String hoscode, String depcode);

    List<DepartmentVo> findDeptTree(String hoscode);

    String getDepName(String hoscode, String depcode);

    Department getDepartment(String hoscode, String depcode);
}