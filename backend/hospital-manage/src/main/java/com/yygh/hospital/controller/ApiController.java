package com.yygh.hospital.controller;

import com.alibaba.fastjson.JSONObject;
import com.yygh.hospital.mapper.HospitalSetMapper;
import com.yygh.hospital.model.HospitalSet;
import com.yygh.common.exception.YyghException;
import com.yygh.common.result.Result;
import com.yygh.hospital.service.ApiService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * 医院后台管理控制器
 * @author XXJ
 */
@Tag(name = "医院管理接口")
@Controller
@RequestMapping
@RequiredArgsConstructor
public class ApiController {

    private static final String MESSAGE_SUCCESS = "操作成功！";
    private static final String MESSAGE_FAILURE = "操作失败！";

    private final ApiService apiService;

    private final HospitalSetMapper hospitalSetMapper;

    @RequestMapping("/hospitalSet/index")
    public String getHospitalSet(ModelMap model, RedirectAttributes redirectAttributes) {
        HospitalSet hospitalSet = hospitalSetMapper.selectById(1);
        model.addAttribute("hospitalSet", hospitalSet);
        return "hospitalSet/index";
    }

    @RequestMapping(value = "/hospitalSet/save")
    public String createHospitalSet(ModelMap model, HospitalSet hospitalSet) {
        hospitalSetMapper.updateById(hospitalSet);
        return "redirect:/hospitalSet/index";
    }

    @RequestMapping("/hospital/index")
    public String getHospital(ModelMap model, RedirectAttributes redirectAttributes) {
        try {
            HospitalSet hospitalSet = hospitalSetMapper.selectById(1);
            if (null == hospitalSet || StringUtils.isEmpty(hospitalSet.getHoscode()) || StringUtils.isEmpty(hospitalSet.getSignKey())) {
                failureMessage("先设置医院code与签名key", redirectAttributes);
                return "redirect:/hospitalSet/index";
            }
            model.addAttribute("hospital", apiService.getHospital());
        } catch (YyghException e) {
            failureMessage(e.getMessage(), redirectAttributes);
            return "redirect:/hospitalSet/index";
        } catch (Exception e) {
            failureMessage("数据异常", redirectAttributes);
            return "redirect:/hospitalSet/index";
        }
        return "hospital/index";
    }

    @RequestMapping(value = "/hospital/create")
    public String createHospital(ModelMap model) {
        return "hospital/create";
    }

    /** 上传医院 - JSON格式 */
    @ResponseBody
    @RequestMapping(value = "/hospital/save", method = RequestMethod.POST)
    public Result saveHospital(@RequestBody Map<String, Object> body) {
        try {
            String data = JSONObject.toJSONString(body.get("data") != null ? body.get("data") : body);
            apiService.saveHospital(data);
        } catch (YyghException e) {
            return Result.fail().message(e.getMessage());
        } catch (Exception e) {
            return Result.fail().message("数据异常");
        }
        return Result.ok().message(MESSAGE_SUCCESS);
    }

    @RequestMapping("/department/list")
    public String findDepartment(ModelMap model,
                                 @RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 RedirectAttributes redirectAttributes) {
        try {
            HospitalSet hospitalSet = hospitalSetMapper.selectById(1);
            if (null == hospitalSet || StringUtils.isEmpty(hospitalSet.getHoscode()) || StringUtils.isEmpty(hospitalSet.getSignKey())) {
                failureMessage("先设置医院code与签名key", redirectAttributes);
                return "redirect:/hospitalSet/index";
            }
            model.addAllAttributes(apiService.findDepartment(pageNum, pageSize));
        } catch (YyghException e) {
            failureMessage(e.getMessage(), redirectAttributes);
            return "redirect:/hospitalSet/index";
        } catch (Exception e) {
            failureMessage("数据异常", redirectAttributes);
            return "redirect:/hospitalSet/index";
        }
        return "department/index";
    }

    @RequestMapping(value = "/department/create")
    public String create(ModelMap model) {
        return "department/create";
    }

    /** 上传科室 - JSON格式 */
    @ResponseBody
    @RequestMapping(value = "/department/save", method = RequestMethod.POST)
    public Result saveDepartment(@RequestBody Map<String, Object> body) {
        try {
            String data = JSONObject.toJSONString(body.get("data") != null ? body.get("data") : body);
            apiService.saveDepartment(data);
        } catch (YyghException e) {
            return Result.fail().message(e.getMessage());
        } catch (Exception e) {
            return Result.fail().message("数据异常");
        }
        return Result.ok().message(MESSAGE_SUCCESS);
    }

    @RequestMapping("/schedule/list")
    public String findSchedule(ModelMap model,
                               @RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(defaultValue = "10") int pageSize,
                               RedirectAttributes redirectAttributes) {
        try {
            HospitalSet hospitalSet = hospitalSetMapper.selectById(1);
            if (null == hospitalSet || StringUtils.isEmpty(hospitalSet.getHoscode()) || StringUtils.isEmpty(hospitalSet.getSignKey())) {
                failureMessage("先设置医院code与签名key", redirectAttributes);
                return "redirect:/hospitalSet/index";
            }
            model.addAllAttributes(apiService.findSchedule(pageNum, pageSize));
        } catch (YyghException e) {
            failureMessage(e.getMessage(), redirectAttributes);
            return "redirect:/hospitalSet/index";
        } catch (Exception e) {
            failureMessage("数据异常", redirectAttributes);
            return "redirect:/hospitalSet/index";
        }
        return "schedule/index";
    }

    @RequestMapping(value = "/schedule/create")
    public String createSchedule(ModelMap model) {
        return "schedule/create";
    }

    /** 上传排班 - JSON格式 */
    @ResponseBody
    @RequestMapping(value = "/schedule/save", method = RequestMethod.POST)
    public Result saveSchedule(@RequestBody Map<String, Object> body) {
        try {
            String data = JSONObject.toJSONString(body.get("data") != null ? body.get("data") : body);
            apiService.saveSchedule(data);
        } catch (YyghException e) {
            return Result.fail().message(e.getMessage());
        } catch (Exception e) {
            return Result.fail().message("数据异常：" + e.getMessage());
        }
        return Result.ok().message(MESSAGE_SUCCESS);
    }

    @RequestMapping(value = "/hospital/createBatch")
    public String createHospitalBatch(ModelMap model) {
        return "hospital/createBatch";
    }

    /** 批量上传医院 - JSON格式 */
    @ResponseBody
    @RequestMapping(value = "/hospital/saveBatch", method = RequestMethod.POST)
    public Result saveBatchHospital(@RequestBody Map<String, Object> body) {
        try {
            apiService.saveBatchHospital();
        } catch (YyghException e) {
            return Result.fail().message(e.getMessage());
        } catch (Exception e) {
            return Result.fail().message("数据异常");
        }
        return Result.ok().message(MESSAGE_SUCCESS);
    }

    @RequestMapping(value = "/department/remove/{depcode}", method = RequestMethod.GET)
    public String removeDepartment(ModelMap model, @PathVariable String depcode, RedirectAttributes redirectAttributes) {
        apiService.removeDepartment(depcode);
        successMessage(null, redirectAttributes);
        return "redirect:/department/list";
    }

    @RequestMapping(value = "/schedule/remove/{hosScheduleId}", method = RequestMethod.GET)
    public String removeSchedule(ModelMap model, @PathVariable String hosScheduleId, RedirectAttributes redirectAttributes) {
        apiService.removeSchedule(hosScheduleId);
        successMessage(null, redirectAttributes);
        return "redirect:/schedule/list";
    }

    private void successMessage(String message, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", StringUtils.isEmpty(message) ? MESSAGE_SUCCESS : message);
        redirectAttributes.addFlashAttribute("messageType", 1);
    }

    private void failureMessage(String message, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", StringUtils.isEmpty(message) ? MESSAGE_FAILURE : message);
        redirectAttributes.addFlashAttribute("messageType", 0);
    }
}
