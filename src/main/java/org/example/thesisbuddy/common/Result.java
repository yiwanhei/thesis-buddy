package org.example.thesisbuddy.common;

import lombok.Data;

@Data
public class Result {
    private int code;           // 状态码：200 成功，400 参数错误，500 系统错误
    private String message;     // 提示信息
    private Object data;        // 业务数据

    // 成功返回（无数据）
    public static Result success() {
        Result result = new Result();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    // 成功返回（带数据）
    public static Result success(Object data) {
        Result result = new Result();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 失败返回
    public static Result error(String message) {
        Result result = new Result();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    // 参数错误
    public static Result badRequest(String message) {
        Result result = new Result();
        result.setCode(400);
        result.setMessage(message);
        return result;
    }
}
