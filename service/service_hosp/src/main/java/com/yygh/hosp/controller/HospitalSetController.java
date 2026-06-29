package com.yygh.hosp.controller;

import com.yygh.common.exception.YyghException;
import com.yygh.common.result.Result;
import com.yygh.common.utils.MD5;
import com.yygh.dto.HospitalSetQueryDTO;
import com.yygh.hosp.service.HospitalSetService;
import com.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Tag(name = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@RequiredArgsConstructor
/**
 * 医院设置控制器
 * @author XXJ
 */
public class HospitalSetController {

    //注入service
    private final HospitalSetService hospitalSetService;

    //1 查询医院设置表所有信息
    @Operation(summary = "获取所有医院设置")
    @GetMapping("findAll")
    public Result findAllHospitalSet() {
        //调用service的方法
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    //2.逻辑删除医院设置
    @Operation(summary = "逻辑删除医院设置")
    @DeleteMapping("{id}")
    public Result removeHospSet(@PathVariable Long id) {
        boolean flag = hospitalSetService.removeById(id);
        if(flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //3 条件查询带分页
    @Operation(summary = "条件查询带分页")
    @PostMapping("findPageHospSet")
    public Result findPageHospSet(@RequestBody HospitalSetQueryDTO dto) {
        //创建page对象，传递当前页，每页记录数
        Page<HospitalSet> page = new Page<>(dto.getPage(), dto.getSize());
        //构建条件
        LambdaQueryWrapper<HospitalSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(dto.getHosname()!=null, HospitalSet::getHosname, dto.getHosname());
        wrapper.eq(dto.getHoscode()!=null, HospitalSet::getHoscode, dto.getHoscode());
        //调用方法实现分页查询
        Page<HospitalSet> pageHospitalSet = hospitalSetService.page(page, wrapper);
        //返回结果
        return Result.ok((IPage<HospitalSet>) pageHospitalSet);
    }

    //4 添加医院设置
    @Operation(summary = "添加医院设置")
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet) {
        //设置状态 1 使用 0 不能使用
        hospitalSet.setStatus(1);
        //签名秘钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));
        //调用service
        boolean save = hospitalSetService.save(hospitalSet);
        if(save) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //5 根据id获取医院设置
    @Operation(summary = "根据id获取医院设置")
    @GetMapping("getHospSet/{id}")
    public Result getHospSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    //6 修改医院设置
    @Operation(summary = "修改医院设置")
    @PostMapping("updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet) {
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if(flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //7 批量删除医院设置
    @Operation(summary = "批量删除医院设置")
    @DeleteMapping("batchRemove")
    public Result batchRemoveHospitalSet(@RequestBody List<Long> idList){
        hospitalSetService.removeByIds(idList);
        return Result.ok();
    }

    //8 医院设置锁定和解锁
    @Operation(summary = "医院设置锁定和解锁")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable Long id,
                                  @PathVariable Integer status) {
        //根据id查询医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //设置状态
        hospitalSet.setStatus(status);
        //调用方法
        hospitalSetService.updateById(hospitalSet);
        return Result.ok();
    }

    //9 发送签名秘钥
    @Operation(summary = "发送签名秘钥")
    @PutMapping("sendKey/{id}")
    public Result lockHospitalSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        //TODO 发送短信
        return Result.ok();
    }
}
