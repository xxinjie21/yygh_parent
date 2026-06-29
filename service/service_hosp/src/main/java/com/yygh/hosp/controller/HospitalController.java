package com.yygh.hosp.controller;

import com.yygh.common.result.Result;
import com.yygh.hosp.service.HospitalService;
import com.yygh.model.hosp.Hospital;
import com.yygh.vo.hosp.HospitalQueryVo;
import com.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "医院管理接口")

@RestController
@RequestMapping("/admin/hosp/hospital")
@RequiredArgsConstructor
/**
 * 医院管理控制器（后台管理）
 * @author XXJ
 */
public class HospitalController {
    private final HospitalService hospitalService;

    //医院列表(条件查询带分页)
    @Operation(summary = "医院列表")
    @GetMapping("list/{page}/{limit}")
    public Result listHosp(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo){
       IPage<Hospital> page1 = hospitalService.selectHospPage(page,limit,hospitalQueryVo);
        List<Hospital> content = page1.getRecords();
        long totalElements = page1.getTotal();
        return Result.ok(page1);
    }

    //更新医院上线状态
    @Operation(summary = "更新上线状态")
    @GetMapping("updateHospStatus/{id}/{status}")
    public Result updateHospStatus(@PathVariable String id,@PathVariable Integer status){
        hospitalService.updateStatus(id,status);
        return Result.ok();
    }

    //医院详情信息
    @Operation(summary = "获取医院详情")
    @GetMapping("showHospDetail/{id}")
    public Result showHospDetail(@PathVariable String id){
        Map<String, Object> map= hospitalService.getHospById(id);
        return Result.ok(map);
    }
}
