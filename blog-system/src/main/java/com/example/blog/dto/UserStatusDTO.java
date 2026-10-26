package com.example.blog.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员修改用户账号状态请求参数
 */
@Data
public class UserStatusDTO implements Serializable {

    /** 账号状态：0-禁用 1-启用 */
    private Integer status;
}
