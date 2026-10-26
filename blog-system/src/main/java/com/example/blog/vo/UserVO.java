package com.example.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息 VO
 * <p>
 * 对外返回用户信息时统一使用本 VO，只包含可公开字段，
 * 不包含 password、salt、deleted 等敏感字段，避免密码/盐值泄露。
 */
@Data
public class UserVO implements Serializable {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 个人简介 */
    private String bio;

    /** 角色：0-普通用户 1-管理员 */
    private Integer role;

    /** 账号状态：0-禁用 1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
