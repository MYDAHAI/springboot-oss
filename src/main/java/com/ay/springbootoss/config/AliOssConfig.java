package com.ay.springbootoss.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;

import static com.ay.springbootoss.enums.AliOssEnum.*;

/**
 * @Author hai-kk
 * @Date 2023/5/28 20:05
 * @Version 1.0
 */
@Data
public class AliOssConfig {
    // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
    static String endpoint = "oss-cn-shenzhen.aliyuncs.com";
    // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
    static String accessKeyId = "LTAI5tNQEQw6KCmevbW1Mc5f";
    static String accessKeySecret = "lf6SOfE3eclSH27Hu7iqOi1ok5U0ke";
    //bucket存储空间名称
    static String bucketName = "anyun-oss";

    public static OSS getOssClient() {
        // 创建OSSClient实例。
        return new OSSClientBuilder().build(END_POINt.getVal(), ACCESS_KEY_ID.getVal(), ACCESS_KEY_SECRET.getVal());
    }

}
