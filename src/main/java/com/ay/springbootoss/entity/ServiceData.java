package com.ay.springbootoss.entity;

import com.ay.springbootoss.enums.ServiceDataCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务数据
 * @Author hai-kk
 * @Date 2023/5/28 22:06
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceData {
    /* 编号 */
    private String code;
    /* 主体 */
    private Object data;
    /* 消息 */
    private String message;

    public static final String AY_A200 = "成功";
    public static final String AY_A401 = "请求参数为空";
    public static final String AY_EXCEPTION = "程序异常";

    public static ServiceData getSuccess() {
        return new ServiceData("200", null, AY_A200);
    }

    public static ServiceData getSuccess(Object object) {
        return new ServiceData("200", object, AY_A200);
    }

    public static ServiceData getException() {
        return new ServiceData("4001", null, AY_EXCEPTION);
    }

    public static ServiceData getException(Object object) {
        return new ServiceData("4001", object, AY_EXCEPTION);
    }

    public static ServiceData getException(ServiceDataCode serviceDataCode) {
        return new ServiceData(serviceDataCode.getCode(), null, serviceDataCode.getMessage());
    }
}
