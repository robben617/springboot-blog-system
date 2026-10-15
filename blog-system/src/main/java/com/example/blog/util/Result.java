package com.example.blog.util;

import lombok.Data;

import java.io.Serializable;

/**
 * 全局统一返回结果封装
 *
 * @param <T> 返回数据类型
 */
@Data
public class Result<T> implements Serializable {

    /** 状态码：200-成功，500-失败，401-未登录 ... */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 返回数据 */
    private T data;

    private Result() {
    }

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        return result;
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（自定义提示 + 数据）
     */
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回（指定状态码）
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    /**
     * 成功返回（自定义提示，无数据）
     */
    public static <T> Result<T> success(String msg) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        return result;
    }

    /**
     * 失败返回（默认500）
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    /**
     * 失败返回（默认500，自定义提示+数据）
     */
    public static <T> Result<T> error(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return Integer.valueOf(200).equals(this.code);
    }
}
