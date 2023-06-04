package com.ay.springbootoss.enums;


/**
 * 阿里oss客户端参数枚举类
 * @Author hai-kk
 * @Date 2023/5/28 21:39
 * @Version 1.0
 */
public enum AliOssEnum {
    /* Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com */
    END_POINt("oss-cn-shenzhen.aliyuncs.com"),
    /* RAM控制台RAM用户的accessKeyId */
    ACCESS_KEY_ID("LTAI5tNQEQw6KCmevbW1Mc5f"),
    /* 创建RAM用户时会出现，注意保存，后续可能无法查看 */
    ACCESS_KEY_SECRET("lf6SOfE3eclSH27Hu7iqOi1ok5U0ke"),
    ;

    private final String val;

    AliOssEnum(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

}
