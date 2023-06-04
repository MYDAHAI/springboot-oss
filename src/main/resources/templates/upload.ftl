<html xmlns:form="http://www.w3.org/1999/html">
    <title>上传文件</title>
    <body><#-- 使用multipart上传文件，请求头中要加这个enctype="multipart/form-data" -->
        <form action="uploadOss" method="post" enctype="multipart/form-data">
            上传到OSS: <input type="file" name="file" /><br>
            <input type="submit" value="上传" />
        </form>
    </body>
</html>
