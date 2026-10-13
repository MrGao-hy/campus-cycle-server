package com.campus.cycle.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传服务（商品图片 / 学生证照片共用）
 */
public interface UploadService {

    /**
     * 校验并保存图片，返回可访问 URL
     * 落盘 campus.upload.dir（按日期分目录 + UUID 文件名）
     */
    String saveImage(MultipartFile file, HttpServletRequest request);
}
