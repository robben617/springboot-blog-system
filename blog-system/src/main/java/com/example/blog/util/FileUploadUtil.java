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
 * 仅允许图片格式，并按用途区分大小限制
 * <p>
 * 大小限制依据《需求分析说明书》「五、业务规则 · 文件上传规则」：
 * 头像单张不超过 2MB，封面单张不超过 5MB；
 * application.yml 中 spring.servlet.multipart.max-file-size=5MB 是 Spring 的硬上限（对应封面），
 * 头像的 2MB 由本工具类在业务层做更严格的校验。
 */
public class FileUploadUtil {

    /** 允许的图片扩展名 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    /** 头像最大大小：2MB */
    public static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024L;

    /** 封面最大大小：5MB */
    public static final long MAX_COVER_SIZE = 5 * 1024 * 1024L;

    private FileUploadUtil() {
    }

    /**
     * 上传头像图片（上限 2MB）
     */
    public static String uploadAvatar(MultipartFile file, String uploadPath) {
        return uploadImage(file, uploadPath, MAX_AVATAR_SIZE, "头像");
    }

    /**
     * 上传封面图片（上限 5MB）
     */
    public static String uploadCover(MultipartFile file, String uploadPath) {
        return uploadImage(file, uploadPath, MAX_COVER_SIZE, "封面");
    }

    /**
     * 上传图片文件（默认按封面上限 5MB 校验，保留原有调用方式）
     *
     * @param file       上传的文件
     * @param uploadPath 上传根目录
     * @return 访问路径（相对路径，用于存储到数据库和前端访问）
     */
    public static String uploadImage(MultipartFile file, String uploadPath) {
        return uploadImage(file, uploadPath, MAX_COVER_SIZE, "图片");
    }

    /**
     * 上传图片文件（自定义大小上限）
     *
     * @param file       上传的文件
     * @param uploadPath 上传根目录（绝对路径或相对路径）
     * @param maxSize    允许的最大字节数
     * @param typeLabel  用途名称，用于错误提示（如「头像」「封面」）
     * @return 访问路径（相对路径，用于存储到数据库和前端访问）
     */
    public static String uploadImage(MultipartFile file, String uploadPath, long maxSize, String typeLabel) {
        // 1. 校验文件非空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        // 2. 校验文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException(typeLabel + "图片大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
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
