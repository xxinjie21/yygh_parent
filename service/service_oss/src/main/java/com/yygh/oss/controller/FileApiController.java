package com.yygh.oss.controller;

import com.yygh.common.result.Result;
import com.yygh.oss.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/oss/file")
@RequiredArgsConstructor
/**
 * 文件上传API控制器
 * @author XXJ
 */
public class FileApiController {

    private final FileService fileService;

    //上传文件到阿里云oss
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) {
        String url = fileService.upload(file);
        return Result.ok(url);
    }
}
