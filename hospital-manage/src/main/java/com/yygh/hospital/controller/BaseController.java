package com.yygh.hospital.controller;


import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.ModelMap;
import java.util.Map;


/**
 * 控制器基类，提供成功/失败提示信息及页面跳转通用方法
 *
 * @author XXJ
 */
public class BaseController {

    //提示信息
    public final static String MESSAGE_SUCCESS = "操作成功！";
    public final static String MESSAGE_FAILURE = "操作失败！";

    /**
     * 成功提示
     *
     * @param message
     * @param redirectAttributes
     */
    protected void successMessage(String message, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", StringUtils.isEmpty(message) ? MESSAGE_SUCCESS : message);
        redirectAttributes.addFlashAttribute("messageType", 1);
    }

    /**
     * 失败提示
     *
     * @param message
     * @param redirectAttributes
     */
    protected void failureMessage(String message, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", StringUtils.isEmpty(message) ? MESSAGE_FAILURE : message);
        redirectAttributes.addFlashAttribute("messageType", 0);
    }

    protected void failureMessage(String message, ModelMap model) {
        model.addAttribute("message", StringUtils.isEmpty(message) ? MESSAGE_SUCCESS : message);
        model.addAttribute("messageType", 0);
    }

    /**
     * 成功页
     *
     * @param message
     * @param request
     */
    protected String successPage(String message, ModelMap model) {
        model.addAttribute("messagePage", StringUtils.isEmpty(message) ? MESSAGE_SUCCESS : message);
        return "common/successPage";
    }

    /**
     * 失败页
     *
     * @param message
     * @param request
     * @return
     */
    protected String failurePage(String message, ModelMap model) {
        model.addAttribute("messagePage", StringUtils.isEmpty(message) ? MESSAGE_FAILURE : message);
        return "common/failurePage";
    }

}
