package com.ay.springbootoss.controller;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.ay.springbootoss.config.AliOssConfig;
import com.ay.springbootoss.entity.ServiceData;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;
import java.util.UUID;

import static com.ay.springbootoss.enums.AliOssEnum.END_POINt;
import static com.ay.springbootoss.utils.DictionaryUtil.BUCKET_NAME;


/**
 * @Author hai-kk
 * @Date 2023/5/28 20:30
 * @Version 1.0
 */
@Api("oss服务")
@RestController
public class OssController {

    @ApiOperation("上传文件")
    @GetMapping("uploadOss")
    public ServiceData uploadOss() {
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。文件会上传到该路径文件中
        String objectName = "anyun/test.txt";
        // 创建OSSClient实例。
        OSS ossClient = AliOssConfig.getOssClient();
        try {
            //上传的内容
            String content = "Hello OSS";
            //上传对象     参数：存储空间名称、上传到(绝对路径)、字节输入流
            ossClient.putObject(BUCKET_NAME, objectName, new ByteArrayInputStream(content.getBytes()));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            return ServiceData.getException(oe);
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            return ServiceData.getException(ce);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ServiceData.getSuccess();
    }

    @ApiOperation("上传文件到OSS---真实访问请求上传")
    @PostMapping("uploadOss")
    public ServiceData uploadOss(@RequestParam("file") MultipartFile file) {
        //获取文件名及后缀信息
        String fileName = file.getOriginalFilename();
        //在文件名称里面添加随机唯一值，使用UUID生成
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        fileName = uuid + fileName;
        String objectName = "anyun/" + fileName;
        //获取文件后缀
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        //设置要上传的文件元数据
        ObjectMetadata objectMetadata = new ObjectMetadata();
        //设置文件内容类型
        objectMetadata.setContentType(getContentType(suffix));
        objectMetadata.setContentDisposition("inline");
        //设置上传文件的访问权限
        objectMetadata.setObjectAcl(CannedAccessControlList.PublicRead);
        //获取oss客户端
        OSS ossClient = AliOssConfig.getOssClient();
        String url;
        try {
            //上传到oss
            ossClient.putObject(BUCKET_NAME, objectName, file.getInputStream(), objectMetadata);
            // 把上传的文件路径返回 （手动拼接）
            // 这里设置图片有效时间 这里设置了一天   (设置私有的情况)
//            Date expiration = new Date(System.currentTimeMillis() + 60 * 60 * 24 * 1000);
//            url = ossClient.generatePresignedUrl(BUCKET_NAME, objectName, expiration).toString();

            //把上传到oss的路径返回   公共访问则直接拼接返回即可
            //需要将路径手动拼接出来，https://xxxxxx.oss-cn-shanghai.aliyuncs.com/edu/avatar/xxxxxx.jpg
            url = "https://"+ BUCKET_NAME + "." + END_POINt.getVal() + "/" + objectName;
        } catch (Exception e) {
            return ServiceData.getException(e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ServiceData.getSuccess(url);
    }

    @ApiOperation("下载oss上的文件")
    @GetMapping("download")
    public ServiceData downloadFile() {
        //要下载的oss上的文件的路径，去掉Bucket名称
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = "anyun/ad78b47dd5e74d80a3cf9e9d6bfb4342990209deaed1472584302c3ef00111d5.jpg";
        //获取oss客户端
        OSS ossClient = AliOssConfig.getOssClient();
        String filePath = "C:\\Users\\hai阳\\Desktop\\oss\\aa.jpg";
        try {
            // 调用ossClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            OSSObject ossObject = ossClient.getObject(BUCKET_NAME, objectName);
            // 调用ossObject.getObjectContent获取文件输入流，可读取此输入流获取其内容。
            InputStream content = ossObject.getObjectContent();
            //没有数据直接返回
            if (content == null) return ServiceData.getSuccess();
            //构建数据输入流
            DataInputStream dataInputStream = new DataInputStream(content);
            //创建数据输出流
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(filePath));
            //每次读1024 * 1024个字节数据
            byte[] bytes = new byte[1024 * 1024];
            while (true) {
                int read = dataInputStream.read(bytes);
                if (read == -1) break;
                dataOutputStream.write(bytes, 0, read);
                dataOutputStream.flush();
            }
            // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
            content.close();
            dataInputStream.close();
            dataOutputStream.close();
        } catch (Exception e) {
            return ServiceData.getException(e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ServiceData.getSuccess();
    }

    @ApiOperation("删除oss上的文件")
    @GetMapping("delete")
    public ServiceData deleteFile() {
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = "anyun/test.txt";
        //获取oss客户端
        OSS ossClient = AliOssConfig.getOssClient();
        try {
            // 删除文件。
            ossClient.deleteObject(BUCKET_NAME, objectName);
        } catch (Exception e) {
            return ServiceData.getException(e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ServiceData.getSuccess();
    }

    @ApiOperation("列举oss上的文件---默认列举100个文件")
    @GetMapping("search")
    public ServiceData searchFile() {
        //获取oss客户端
        OSS ossClient = AliOssConfig.getOssClient();
        try {
            // ossClient.listObjects返回ObjectListing实例，包含此次listObject请求的返回结果。
            ObjectListing objectListing = ossClient.listObjects(BUCKET_NAME);
            // objectListing.getObjectSummaries获取所有文件的描述信息。
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                //objectSummary.getKey()获取文件去掉Bucket存储空间名称的绝对路径  例：anyun/aac/ddde90e6015a490291f229fc0ff8760amoney.png
                System.out.println(" ： " + objectSummary.getKey() + "  " +
                        "(size = " + objectSummary.getSize() + ")");
            }
        } catch (Exception e) {
            return ServiceData.getException(e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ServiceData.getSuccess();
    }


    public static String getContentType(String filenameExtension) {
        if (filenameExtension.equalsIgnoreCase(".pdf")) {
            return "application/pdf";
        }
        if (filenameExtension.equalsIgnoreCase(".bmp")) {
            return "image/bmp";
        }
        if (filenameExtension.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (filenameExtension.equalsIgnoreCase(".jpeg") ||
                filenameExtension.equalsIgnoreCase(".jpg") ||
                filenameExtension.equalsIgnoreCase(".png")) {
            return "image/jpg";
        }
        if (filenameExtension.equalsIgnoreCase(".html")) {
            return "text/html";
        }
        if (filenameExtension.equalsIgnoreCase(".txt")) {
            return "text/plain";
        }
        if (filenameExtension.equalsIgnoreCase(".vsd")) {
            return "application/vnd.visio";
        }
        if (filenameExtension.equalsIgnoreCase(".pptx") ||
                filenameExtension.equalsIgnoreCase(".ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (filenameExtension.equalsIgnoreCase(".docx"))
        {
            return "application/msword";
        }
        if (filenameExtension.equalsIgnoreCase(".xml")) {
            return "text/xml";
        }
        return "image/jpg";
    }


}
