package com.yygh.hospital.controller;

import com.alibaba.fastjson.JSONObject;
import com.yygh.hospital.dto.SubmitOrderDTO;
import com.yygh.hospital.dto.UpdateCancelStatusDTO;
import com.yygh.hospital.dto.UpdatePayStatusDTO;
import com.yygh.common.exception.YyghException;
import com.yygh.common.result.Result;
import com.yygh.common.result.ResultCodeEnum;
import com.yygh.hospital.service.ApiService;
import com.yygh.hospital.service.HospitalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 *
 * @author XXJ
 *
 */
@Tag(name = "医院管理接口")
@RestController
@RequiredArgsConstructor
public class HospitalController {

	private final HospitalService hospitalService;

	private final ApiService apiService;

	/** 预约下单 */
	@PostMapping("/order/submitOrder")
	public Result AgreeAccountLendProject(@RequestBody SubmitOrderDTO dto) {
		try {
			Map<String, Object> paramMap = JSONObject.parseObject(JSONObject.toJSONString(dto), Map.class);
			if (!com.yygh.hospital.util.HttpRequestHelper.isSignEquals(paramMap, apiService.getSignKey())) {
				throw new YyghException(ResultCodeEnum.SIGN_ERROR);
			}
			Map<String, Object> resultMap = hospitalService.submitOrder(paramMap);
			return Result.ok(resultMap);
		} catch (YyghException e) {
			return Result.fail().message(e.getMessage());
		}
	}

	/** 更新支付状态 */
	@PostMapping("/order/updatePayStatus")
	public Result updatePayStatus(@RequestBody UpdatePayStatusDTO dto) {
		try {
			Map<String, Object> paramMap = JSONObject.parseObject(JSONObject.toJSONString(dto), Map.class);
			if (!com.yygh.hospital.util.HttpRequestHelper.isSignEquals(paramMap, apiService.getSignKey())) {
				throw new YyghException(ResultCodeEnum.SIGN_ERROR);
			}
			hospitalService.updatePayStatus(paramMap);
			return Result.ok();
		} catch (YyghException e) {
			return Result.fail().message(e.getMessage());
		}
	}

	/** 更新取消预约状态 */
	@PostMapping("/order/updateCancelStatus")
	public Result updateCancelStatus(@RequestBody UpdateCancelStatusDTO dto) {
		try {
			Map<String, Object> paramMap = JSONObject.parseObject(JSONObject.toJSONString(dto), Map.class);
			if (!com.yygh.hospital.util.HttpRequestHelper.isSignEquals(paramMap, apiService.getSignKey())) {
				throw new YyghException(ResultCodeEnum.SIGN_ERROR);
			}
			hospitalService.updateCancelStatus(paramMap);
			return Result.ok();
		} catch (YyghException e) {
			return Result.fail().message(e.getMessage());
		}
	}
}
