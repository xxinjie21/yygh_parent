package com.yygh.hosp.controller;

import com.yygh.common.result.Result;
import com.yygh.dto.HospitalQueryDTO;
import com.yygh.hosp.service.HospitalService;
import com.yygh.vo.hosp.HospitalVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yygh.model.hosp.Hospital;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("list")
    public Result listHosp(@RequestBody HospitalQueryDTO dto){
       IPage<Hospital> page1 = hospitalService.selectHospPage(dto);
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
        HospitalVo vo = hospitalService.getHospById(id);
        return Result.ok(vo);
    }
}
