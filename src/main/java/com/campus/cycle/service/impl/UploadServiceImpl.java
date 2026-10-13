package com.campus.cycle.service.impl;

import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 图片上传服务实现
 */
@Service
public class UploadServiceImpl implements UploadService {

    /** 允许的图片类型 */
    private static final Set<String> ALLOWED_TYPES = Set.of("jpg", "jpeg", "png", "webp", "gif");

    /** 上传目录（application.yml: campus.upload.dir） */
    @Value("${campus.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public String saveImage(MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED_TYPES.contains(ext)) {
            throw new BusinessException("仅支持 jpg/png/webp/gif 图片");
        }
        // 按日期分目录，避免单目录文件过多
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File dir = new File(uploadDir, datePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("上传目录创建失败");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File dest = new File(dir, filename);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("图片保存失败，请重试");
        }
        // 基于请求 Host 动态拼 URL：H5/开发者工具/真机预览均可直接访问
        String host = request.getHeader("Host");
        return (host != null ? "http://" + host : "") + "/uploads/" + datePath + "/" + filename;
    }
}
