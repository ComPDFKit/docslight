package com.compdf.config.base;


import cn.hutool.http.HttpStatus;
import com.compdf.enums.ErrorInfoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 请求统一返回对象
 * @author WPH 2022/7/5
 */
@Data
@AllArgsConstructor
public class R<T> {

    private int code;
    private String message;
    private T data;

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        return ok("success", data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return ok(HttpStatus.HTTP_OK + "", msg, data);
    }

    public static <T> R<T> ok(String code, String msg) {
        return new R<>(Integer.parseInt(code), msg, null);
    }

    public static <T> R<T> ok(String code, String msg, T data) {
        return new R<>(Integer.parseInt(code), msg, data);
    }

    public static <T> R<T> error() {
        return error("error");
    }

    public static <T> R<T> error(String msg) {
        return error(ErrorInfoEnum.ERROR_INNER.getCode(), msg);
    }

    public static <T> R<T> error(ErrorInfoEnum errorEnum, T data) {
        return error(errorEnum.getCode(), errorEnum.getZhMsg(), data);
    }

    public static <T> R<T> error(String code, String msg) {
        return error(code, msg, null);
    }

    public static <T> R<T> error(String code, String msg, T data) {
        return new R<>(Integer.parseInt(code), msg, data);
    }

}
