package com.example.blog.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改个人资料请求参数
 * <p>
 * 所有字段均为可选：只传需要修改的字段，未传（null）的字段保持原值不变；
 * 昵称若传了则不能为空白；邮箱/头像/简介传空串表示清空该字段。
 */
@Data
public class UpdateProfileDTO implements Serializable {

    /** 昵称（可选，不超过50字符） */
    private String nickname;

    /** 邮箱（可选，不超过100字符） */
    private String email;

    /** 头像URL（可选，不超过255字符） */
    private String avatar;

    /** 个人简介（可选，不超过300字符） */
    private String bio;
}
