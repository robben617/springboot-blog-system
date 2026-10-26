package com.example.blog.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码请求参数
 */
@Data
public class ChangePasswordDTO implements Serializable {

    /** 原密码（明文） */
    private String oldPassword;

    /** 新密码（明文，至少6位，与注册规则一致） */
    private String newPassword;
}
