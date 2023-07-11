package com.ay.springbootoss.controller;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.ay.springbootoss.config.AliOssConfig;
import com.ay.springbootoss.entity.ServiceData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.ay.springbootoss.constant.DictionaryConstant.BUCKET_NAME;
import static com.ay.springbootoss.constant.DictionaryConstant.END_POINt;


/**
 * @Author hai-kk
 * @Date 2023/5/28 20:30
 * @Version 1.0
 */
@Api("oss服务")
@RestController
public class OssController {

    @ApiOperation("上传回调")
    @PostMapping("uploadCallback")
    public ServiceData uploadCallback(String name, String size, String mimeType) {
        System.out.println("--------回调成功----name: " + name + "---size: " + size + "---mimeType: " + mimeType);
        return ServiceData.getSuccess("回调成功");
    }

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
            url = "https://"+ BUCKET_NAME + "." + END_POINt + "/" + objectName;
        } catch (Exception e) {
            return ServiceData.getException(e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ServiceData.getSuccess(url);
    }


    @ApiOperation("断点续传---包含上传回调")
    @PostMapping("uploadContinued")
    public ServiceData uploadContinued(@RequestParam("file") MultipartFile file) {
        //获取文件后缀
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        //获取文件名及后缀信息
        String fileName = file.getOriginalFilename();
        //在文件名称里面添加随机唯一值，使用UUID生成
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        fileName = "duan" + uuid + fileName;
        String objectName = "anyun/aac/" + fileName;
        //获取oss客户端
        OSS ossClient = AliOssConfig.getOssClient();
        //最终要上传的本地文件路径
        String filePath = "E:\\b\\" + fileName;


        File file1 = new File(filePath);
        //文件目录不存在就创建
        if (!file1.getParentFile().exists()) {
            if (!file1.getParentFile().mkdirs()) return ServiceData.getException("本地文件目录创建失败");
        }
        try {
            //将MultipartFile中的文件内容存到本地的该路径文件中
            file.transferTo(file1);

            ObjectMetadata meta = new ObjectMetadata();
            // 指定上传的内容类型。
            meta.setContentType(getContentType(suffix));
            // 文件上传时设置访问权限ACL。
            meta.setObjectAcl(CannedAccessControlList.PublicRead);

            // 通过UploadFileRequest设置多个参数。
            // 依次填写Bucket名称（例如examplebucket）以及Object完整路径（例如exampledir/exampleobject.txt），Object完整路径中不能包含Bucket名称。
            UploadFileRequest uploadFileRequest =
                    new UploadFileRequest(BUCKET_NAME,objectName);

            // 通过UploadFileRequest设置单个参数。
            // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
            //上传本地的该路径的文件
            uploadFileRequest.setUploadFile(filePath);

            // 指定上传并发线程数，默认值为1。
            uploadFileRequest.setTaskNum(5);
            // 指定上传的分片大小，单位为字节，取值范围为100 KB~5 GB。默认值为100 KB。
            uploadFileRequest.setPartSize(100);
            // 开启断点续传，默认关闭。
            uploadFileRequest.setEnableCheckpoint(true);
            // 记录本地分片上传结果的文件。上传过程中的进度信息会保存在该文件中，如果某一分片上传失败，再次上传时会根据文件中记录的点继续上传。上传完成后，该文件会被删除。
            // 如果未设置该值，默认与待上传的本地文件同路径，名称为${uploadFile}.ucp。
            uploadFileRequest.setCheckpointFile("E:\\b\\uploadFile");
            // 文件的元数据。
            uploadFileRequest.setObjectMetadata(meta);

            // 上传回调参数。
            Callback callback = new Callback();
            //设置回调地址  该url的服务必须是post方法，且返回类型要是json格式
            callback.setCallbackUrl("http://jndp85.natappfree.cc//uploadCallback");

            String body = "{\"mimeType\":\"${mimeType}\",\"size\":\"${size}\"}";
//            String body = "bucket=${bucket}&object=${object}&etag=${etag}&size=${size}&mimeType=${mimeType}";
            BASE64Encoder encoder = new BASE64Encoder();
            String encodeBody = encoder.encode(body.getBytes(StandardCharsets.UTF_8));
//            encoder.encode();
            // 设置发起回调时请求body的值。需要进行base64编码
            callback.setCallbackBody(encodeBody);
            // 设置发起回调请求的Content-Type。
            callback.setCalbackBodyType(Callback.CalbackBodyType.JSON);
            // 设置发起回调请求的自定义参数，由Key和Value组成，Key必须以x:开始。
            callback.addCallbackVar("x:name", "value1");

            // 设置上传回调，参数为Callback类型。
            uploadFileRequest.setCallback(callback);

            // 断点续传上传。
            UploadFileResult uploadFileResult = ossClient.uploadFile(uploadFileRequest);

            // 读取上传回调返回的消息内容。
            byte[] buffer = new byte[43 + (6 * 2)];
            CompleteMultipartUploadResult multipartUploadResult = uploadFileResult.getMultipartUploadResult();
            InputStream inputStream = multipartUploadResult.getResponse().getContent();
            int read = inputStream.read(buffer);
            System.out.println("打印回调返回的内容：" + new String(buffer));
            // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
            multipartUploadResult.getResponse().getContent().close();

            //上传后将本地存的文件删除
            if (!file1.delete()) return ServiceData.getException("删除本地文件失败");

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            // 关闭OSSClient。
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ServiceData.getSuccess();
    }

    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<String, String>(){{
            put("name", "${aaaa}");
            put("age", "${bbbb}");
        }};

//        System.out.println(JSONObject.toJSONString(map));
        String body = "bucket=${bucket}&object=${object}&etag=${etag}&size=${size}&mimeType=${mimeType}";
        BASE64Encoder encoder = new BASE64Encoder();
        String encode = encoder.encode(body.getBytes(StandardCharsets.UTF_8));
        System.out.println(encode);

        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] bytes = decoder.decodeBuffer(encode);
            System.out.println(new String(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @ApiOperation("分片上传")
    @PostMapping("shardingUpload")
    public ServiceData shardingUpload(@RequestParam("file") MultipartFile file) {
        //获取文件后缀
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        //获取文件名及后缀信息
        String fileName = file.getOriginalFilename();
        //在文件名称里面添加随机唯一值，使用UUID生成
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        fileName = uuid + fileName;
        String objectName = "anyun/aac/" + fileName;

        //最终要上传的本地文件路径
        String filePath = "E:\\b\\" + fileName;

        File sampleFile = new File(filePath);
        //文件目录不存在就创建
        if (!sampleFile.getParentFile().exists()) {
            if (!sampleFile.getParentFile().mkdirs()) return ServiceData.getException("本地文件目录创建失败");
        }

        //获取oss客户端
        OSS ossClient = AliOssConfig.getOssClient();
        try {
            //将MultipartFile中的文件内容存到本地的该路径文件中
            file.transferTo(sampleFile);

            // 创建InitiateMultipartUploadRequest对象。
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(BUCKET_NAME, objectName);

            // 如果需要在初始化分片时设置请求头，请参考以下示例代码。
             ObjectMetadata metadata = new ObjectMetadata();
             metadata.setContentType(getContentType(suffix));
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // 指定该Object的网页缓存行为。
            // metadata.setCacheControl("no-cache");
            // 指定该Object被下载时的名称。
            // metadata.setContentDisposition("attachment;filename=oss_MultipartUpload.txt");
            // 指定该Object的内容编码格式。
            // metadata.setContentEncoding(OSSConstants.DEFAULT_CHARSET_NAME);
            // 指定初始化分片上传时是否覆盖同名Object。此处设置为true，表示禁止覆盖同名Object。
            // metadata.setHeader("x-oss-forbid-overwrite", "true");
            // 指定上传该Object的每个part时使用的服务器端加密方式。
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
            // 指定Object的加密算法。如果未指定此选项，表明Object使用AES256加密算法。
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_DATA_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
            // 指定KMS托管的用户主密钥。
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION_KEY_ID, "9468da86-3509-4f8d-a61e-6eab1eac****");
            // 指定Object的存储类型。
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard);
            // 指定Object的对象标签，可同时设置多个标签。
            // metadata.setHeader(OSSHeaders.OSS_TAGGING, "a:1");
             request.setObjectMetadata(metadata);


            // 初始化分片。
            InitiateMultipartUploadResult uploadResult = ossClient.initiateMultipartUpload(request);
            // 返回uploadId，它是分片上传事件的唯一标识。您可以根据该uploadId发起相关的操作，例如取消分片上传、查询分片上传等。
            String uploadId = uploadResult.getUploadId();

            // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
            List<PartETag> partETags =  new ArrayList<PartETag>();
            // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
            final long partSize = 1024 * 100L;   //1 MB。

            // 根据上传的数据大小计算分片数。以本地文件为例，说明如何通过File.length()获取上传数据的大小。
//            final File sampleFile = new File("D:\\localpath\\examplefile.txt");
            long fileLength = sampleFile.length();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }
            // 遍历分片上传。
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(BUCKET_NAME);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                // 设置上传的分片流。
                // 以本地文件为例说明如何创建FIleInputstream，并通过InputStream.skip()方法跳过指定字节数据。
                InputStream inputStream = new FileInputStream(sampleFile);
                inputStream.skip(startPos);
                uploadPartRequest.setInputStream(inputStream);
                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
                uploadPartRequest.setPartNumber( i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
                partETags.add(uploadPartResult.getPartETag());
            }


            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(BUCKET_NAME, objectName, uploadId, partETags);
            //设置组成后的文件访问权限
            completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.PublicRead);

            // 如果需要在完成分片上传的同时设置文件访问权限，请参考以下示例代码。
            // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.Private);
            // 指定是否列举当前UploadId已上传的所有Part。仅在Java SDK为3.14.0及以上版本时，支持通过服务端List分片数据来合并完整文件时，将CompleteMultipartUploadRequest中的partETags设置为null。
            // Map<String, String> headers = new HashMap<String, String>();
            // 如果指定了x-oss-complete-all:yes，则OSS会列举当前UploadId已上传的所有Part，然后按照PartNumber的序号排序并执行CompleteMultipartUpload操作。
            // 如果指定了x-oss-complete-all:yes，则不允许继续指定body，否则报错。
            // headers.put("x-oss-complete-all","yes");
            // completeMultipartUploadRequest.setHeaders(headers);

            // 完成分片上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            System.out.println(completeMultipartUploadResult.getETag());
        } catch (IOException | OSSException oe) {
            oe.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ServiceData.getSuccess();
    }

    @ApiOperation("取消分片上传")
    @GetMapping("cancelShardingUpload")
    public ServiceData cancelShardingUpload() {
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = "exampledir/exampleobject.txt";
        // 填写uploadId，例如0004B999EF518A1FE585B0C9360D****。uploadId来自于InitiateMultipartUpload返回的结果。
        String uploadId = "0004B999EF518A1FE585B0C9360D****";

        // 创建OSSClient实例。
        OSS ossClient = AliOssConfig.getOssClient();
        try {
            // 取消分片上传。
            AbortMultipartUploadRequest abortMultipartUploadRequest =
                    new AbortMultipartUploadRequest(BUCKET_NAME, objectName, uploadId);
            ossClient.abortMultipartUpload(abortMultipartUploadRequest);
        } catch (OSSException oe) {
            oe.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ServiceData.getSuccess();
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


    /**
     * 根据文件后缀匹配对应的文件类型
     * @param filenameExtension
     * @return String
     * @Author anyun
     * @Date 2023/6/10 20:54
     */
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
