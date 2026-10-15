package com.example.blog.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 密码加密工具类
 * 采用 MD5 + 随机盐 的方式加密密码，不引入 Spring Security
 * 存储格式：盐值 + MD5(盐值 + 明文密码)
 */
public class PasswordUtil {

    /** 盐值长度 */
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {
    }

    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, SALT_LENGTH);
    }

    /**
     * 对密码进行加密
     *
     * @param plainPassword 明文密码
     * @param salt          盐值
     * @return 加密后的密码（MD5）
     */
    public static String encrypt(String plainPassword, String salt) {
        String raw = salt + plainPassword;
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验密码是否匹配
     *
     * @param plainPassword 用户输入的明文密码
     * @param salt          数据库存储的盐值
     * @param cipherPassword 数据库存储的加密密码
     * @return 是否匹配
     */
    public static boolean matches(String plainPassword, String salt, String cipherPassword) {
        String encrypted = encrypt(plainPassword, salt);
        return encrypted.equals(cipherPassword);
    }
}
