package com.example.blog.common;

/**
 * 系统常量定义
 */
public class Constants {

    /** Session 中存储的用户ID键名 */
    public static final String SESSION_USER_ID = "userId";

    /** Session 中存储的用户名键名 */
    public static final String SESSION_USERNAME = "username";

    /** Session 中存储的用户角色键名 */
    public static final String SESSION_ROLE = "role";

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 默认分页页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    // ==================== 角色常量 ====================
    /** 普通用户 */
    public static final int ROLE_USER = 0;

    /** 管理员 */
    public static final int ROLE_ADMIN = 1;

    // ==================== 账号状态常量 ====================
    /** 账号禁用 */
    public static final int STATUS_DISABLED = 0;

    /** 账号启用 */
    public static final int STATUS_ENABLED = 1;

    // ==================== 文章状态常量 ====================
    /** 草稿 */
    public static final int ARTICLE_DRAFT = 0;

    /** 已发布 */
    public static final int ARTICLE_PUBLISHED = 1;

    // ==================== 常用返回状态码 ====================
    /** 成功 */
    public static final int CODE_SUCCESS = 200;
    /** 请求参数有误 */
    public static final int CODE_BAD_REQUEST = 400;
    /** 未登录 */
    public static final int CODE_UNAUTHORIZED = 401;
    /** 无权限 */
    public static final int CODE_FORBIDDEN = 403;
    /** 资源不存在 */
    public static final int CODE_NOT_FOUND = 404;
    /** 请求方法不支持 */
    public static final int CODE_METHOD_NOT_ALLOWED = 405;
    /** 系统错误 */
    public static final int CODE_SERVER_ERROR = 500;
}
