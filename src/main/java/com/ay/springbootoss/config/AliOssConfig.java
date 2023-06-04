package com.ay.springbootoss.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;

import static com.ay.springbootoss.constant.DictionaryConstant.*;

/**
 * @Author hai-kk
 * @Date 2023/5/28 20:05
 * @Version 1.0
 */
@Data
public class AliOssConfig {

    public static OSS getOssClient() {
        // 创建OSSClient实例。
        return new OSSClientBuilder().build(END_POINt, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
    }

}
