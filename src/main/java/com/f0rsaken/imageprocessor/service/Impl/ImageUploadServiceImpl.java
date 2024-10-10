package com.f0rsaken.imageprocessor.service.Impl;

import com.f0rsaken.imageprocessor.common.BaseResponse;
import com.f0rsaken.imageprocessor.common.ResponseCode;
import com.f0rsaken.imageprocessor.config.FileConfig;
import com.f0rsaken.imageprocessor.exception.FileUploadException;
import com.f0rsaken.imageprocessor.service.ImageUploadService;
import com.f0rsaken.imageprocessor.utils.RabbitMQUtil;
import com.f0rsaken.imageprocessor.utils.TencentCOSUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.f0rsaken.imageprocessor.common.ResponseCode.*;

@Slf4j
@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    @Autowired
    private FileConfig fileConfig;

    @Autowired
    private TencentCOSUtil tencentCOSUtil;

    @Override
    public String uploadToLocal(MultipartFile file) {

        // 可以在这里进行文件的初步验证，比如文件大小、类型等
        boolean success = isValidateFile(file);
        if (!success) {
            log.error("文件验证失败");
            throw new FileUploadException("文件验证失败", INVALID_FILE);
        }

        // 获取临时文件存储目录
        File dir = fileConfig.getTempDir();
        if (!dir.exists() && !dir.mkdirs()) {
            log.error("临时文件目录创建失败: {}", dir.getAbsolutePath());
            throw new FileUploadException("临时文件目录创建失败", INTERNAL_SERVER_ERROR);
        }

        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "-" + originalFileName;  // 生成唯一文件名
        File tempFile = new File(dir, uniqueFileName);  // 在指定目录下创建临时文件

        try {
            file.transferTo(tempFile);  // 将 MultipartFile 写入到临时文件
        } catch (IOException e) {
            log.error("临时文件创建失败: {}", e.getMessage());
            throw new FileUploadException("临时文件创建失败", INTERNAL_SERVER_ERROR);
        }

        log.info("文件成功上传到本地: {}, 原文件名: {}", tempFile.getAbsolutePath(), originalFileName);
        return tempFile.getName();

        // 发布到RabbitMQ，处理文件转换逻辑
        // rabbitMQUtil.sendToQueue(cosFilePath);
    }

    public String uploadToCOS(MultipartFile file) {
        // 实现上传文件到COS的逻辑
        // 返回上传到COS后的路径

        COSClient cosClient = tencentCOSUtil.getCosClient();

        // 为上传的文件生成唯一名称
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "-" + originalFileName;
        String objectKey = tencentCOSUtil.getUploadFolder() + uniqueFileName; // COS中的上传路径

        // 创建 ObjectMetadata 对象并设置内容长度
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize()); // 设置文件大小

        try {
            cosClient.putObject(tencentCOSUtil.getBucketName(), objectKey, file.getInputStream(), metadata);
        } catch (CosServiceException e) {
            log.error("COS服务端异常：{}", e.getMessage());
            throw new RuntimeException("文件上传到COS失败 - 服务端异常", e);
        } catch (CosClientException e) {
            log.error("COS客户端异常：{}", e.getMessage());
            throw new RuntimeException("文件上传到COS失败 - 客户端异常", e);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传过程中发生IO异常", e);
        }

        // 检查输入文件是否存在
        try {
            boolean objectExists = cosClient.doesObjectExist(tencentCOSUtil.getBucketName(), objectKey);
            if (!objectExists) {
                log.error("临时文件不存在: {}", objectKey);
                throw new FileUploadException("临时文件不存在");
            }
        } catch (CosServiceException e) {
            log.error("客户端异常: {}", e.getMessage());
            throw new FileUploadException("文件检查失败: 客户端异常", e);
        } catch (CosClientException e) {
            log.error("服务端异常：{}", e.getMessage());
            throw new FileUploadException("文件检查失败: 服务端异常", e);
        }

        return cosClient.getObjectUrl(tencentCOSUtil.getBucketName(), objectKey).toString();
    }


    private Boolean isValidateFile(MultipartFile file) {

        // 文件大小检查
        if (!isFileSizeValid(file)) {
            return false;
        }

        // 内容检查
        if (!isValidImageFile(file)) {
            return false; // 内容检查失败
        }

        // 扩展名和MIME类型检查
        if (!isValidateFileType(file)) {
            return false;
        }

        // 所有检查通过
        return true;
    }

    private Boolean isFileSizeValid(MultipartFile file) {

        // 检查文件大小
        if (file.getSize() > fileConfig.getMaxFileSize()) {
            log.error("文件大小超过最大限制：{}", file.getOriginalFilename());
            return false;
        }

        return true;
    }


    private Boolean isValidImageFile(MultipartFile file) {
        try {
            // 尝试读取图像
            ImageIO.read(file.getInputStream());
            return true; // 如果能读取，则是有效的图像
        } catch (IOException e) {
            log.error("无法读取图像文件：{}", e.getMessage());
            return false; // 读取失败，文件可能无效
        }
    }

    private Boolean isValidateFileType(MultipartFile file) {

        // 获取文件的扩展名
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            log.error("文件名无效：{}", fileName);
            return false;
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        // 检查扩展名是否在白名单中
        if (!fileConfig.getAllowedExtensions().contains(extension)) {
            log.error("不支持的文件扩展名：{}", extension);
            return false;
        }

        // 使用 Apache Tika 检测 MIME 类型
        Tika tika = new Tika();
        String mimeType;
        try (InputStream inputStream = file.getInputStream()) {
            mimeType = tika.detect(inputStream);
        } catch (IOException e) {
            log.error("获取 MIME 类型时出错: {}", e.getMessage());
            return false;
        }

        // 检查MIME类型是否在白名单中
        if (!fileConfig.getAllowedMimeTypes().contains(mimeType)) {
            log.error("不支持的文件MIME类型：{}", mimeType);
            return false;
        }

        return true;
    }
}
