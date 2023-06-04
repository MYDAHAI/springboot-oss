package com.ay.springbootoss.enums;


/**
 * 服务数据CODE类
 * @Author hai-kk
 * @Date 2023/5/28 22:06
 * @Version 1.0
 */
public enum ServiceDataCode {
    AY_A401("401", "请求参数为空"),
    ;

    /* 编号 */
    private final String code;
    /* 消息 */
    private final String message;

    ServiceDataCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
