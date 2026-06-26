package com.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 * @author XXJ
 */
public interface FileService {
    //上传文件到阿里云oss
    String upload(MultipartFile file);
}
