package com.example.blog.util;

import com.example.blog.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传工具类
 * 仅允许图片格式，限制文件大小
 */
public class FileUploadUtil {

    /** 允许的图片扩展名 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    /** 单个文件最大大小：2MB */
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024L;

    private FileUploadUtil() {
    }

    /**
     * 上传图片文件
     *
     * @param file        上传的文件
     * @param uploadPath  上传根目录（绝对路径）
     * @return 访问路径（相对路径，用于存储到数据库和前端访问）
     */
    public static String uploadImage(MultipartFile file, String uploadPath) {
        // 1. 校验文件非空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        // 2. 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过2MB");
        }
        // 3. 校验文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.lastIndexOf(".") == -1) {
            throw new BusinessException("文件名格式不正确");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("仅支持 jpg、jpeg、png、gif、webp 格式的图片");
        }
        // 4. 生成存储路径（按日期分目录）
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dirPath = uploadPath + File.separator + dateDir;
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("创建上传目录失败");
        }
        // 5. 生成新文件名（UUID + 扩展名）
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File destFile = new File(dir, newFilename);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }
        // 6. 返回相对访问路径（/upload/yyyy/MM/dd/xxx.jpg）
        return "/upload/" + dateDir + "/" + newFilename;
    }
}
