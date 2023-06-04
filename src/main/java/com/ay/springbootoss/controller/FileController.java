package com.ay.springbootoss.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 上传下载示例代码
 * @Author hai-kk
 * @Date 2023/5/28 23:02
 * @Version 1.0
 */
@Controller
public class FileController {

    @ApiOperation("跳转到upload页面")
    @GetMapping("byUpload")
    public String byUpload() {
        return "upload";
    }

    //上传文件(可多个文件上传)
    @RequestMapping("/files")
    public String fhah(@RequestParam("name") String name, @RequestParam("file") MultipartFile[] files) throws IOException {
        for (MultipartFile file : files) {


//            //获取文件名及后缀信息    money.png
//            String fileName = file.getOriginalFilename();
//            //获取文件后缀   .png
//            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
//            //获取文件类型   image/png
//            String fileType = file.getContentType();
//            //获取文件大小   394187
//            long fileSize = file.getSize();
//            System.out.println("获取文件名及后缀信息: " + fileName);
//            System.out.println("获取文件后缀: " + suffix);
//            System.out.println("获取文件类型: " + fileType);
//            System.out.println("获取文件大小: " + fileSize);

            //		if(!file.isEmpty()) {
            //			file.transferTo(fil);//简单写法
            //		}

            File fil = new File("C:\\Users\\hai阳\\Desktop\\aaa\\" +System.currentTimeMillis() + file.getOriginalFilename());

            InputStream is = file.getInputStream();
            OutputStream os = new FileOutputStream(fil);

            int read = 0;
            byte[] bytes = new byte[2048];
            while ((read = is.read(bytes)) != -1)
                os.write(bytes, 0, read);
            is.close();
            os.close();
        }
        return "redirect:emps";
    }


    //可以下载任何文件
    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response, @RequestParam("filePath") String path) throws IOException{
        System.out.println(path);

        String paths[] = path.split("\\\\");
        String fileName = paths[paths.length - 1];
        File file = new File(path);

        //返回头信息
        response.setHeader("Content-Disposition","attachment;filename=" + fileName);
        response.addHeader("Content-Type","application/json;charset=UTF-8");

        //try的括号中所有实现Closeable的类声明都可以写在里面，最常见的是流操作，socket操作等。括号中可以写多行语句，会自动关闭括号中的资源。
        try(InputStream is = new FileInputStream(file); OutputStream os = response.getOutputStream();)
        {
            int read = 0;
            byte[] bytes = new byte[2048];
            while ((read = is.read(bytes)) != -1)
                os.write(bytes, 0, read);
        }
    }

    //只能下载小文件
    @RequestMapping("/down")
    public ResponseEntity<byte[]> downs() {
        //双磁盘把文件读到当前这个方法里面　
        byte [] body = null;
        InputStream in;
        try {
            in = new FileInputStream(new File("C:\\Users\\hai阳\\Desktop\\a.txt"));
            body = new byte[in.available()];
            in.read(body);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=kkk.txt");

        HttpStatus statusCode = HttpStatus.OK;

        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(body, headers, statusCode);
        return response;
    }

}
