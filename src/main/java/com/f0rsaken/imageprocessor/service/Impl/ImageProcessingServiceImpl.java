package com.f0rsaken.imageprocessor.service.Impl;

import com.f0rsaken.imageprocessor.common.BaseResponse;
import com.f0rsaken.imageprocessor.config.FileConfig;
import com.f0rsaken.imageprocessor.service.ImageProcessingService;
import com.f0rsaken.imageprocessor.utils.TencentCOSUtil;
import com.qcloud.cos.COSClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.im4java.core.IMOperation;
import org.im4java.core.ImageMagickCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

import static com.f0rsaken.imageprocessor.common.ResponseCode.*;

@Slf4j
@Service
public class ImageProcessingServiceImpl implements ImageProcessingService {

    @Autowired
    private FileConfig fileConfig;

    @Autowired
    private TencentCOSUtil tencentCOSUtil;

    // 定义输出文件的固定路径
    private static final String OUTPUT_DIR = "D:/project/imageProcessor/src/main/resources/outputFile/";


    /**
     * 转换文件格式
     * @param file
     * @param format
     * @return
     */
    @Override
    public BaseResponse<String> convertImageFormat(MultipartFile file, String format) {

        boolean success = isValidateFile(file);
        if (!success) {
            return BaseResponse.error(INVALID_FILE);
        }

        COSClient cosClient = tencentCOSUtil.getCosClient();

        // 为上传的文件生成唯一名称
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "-" + originalFileName;

        // 上传文件到腾讯云COS
        String uploadPath = tencentCOSUtil.getUploadFolder() + uniqueFileName; // COS中的上传路径
        try {
           cosClient.putObject(tencentCOSUtil.getBucketName(), uploadPath, file.getInputStream(), null);
        } catch (IOException e) {
            log.error("文件上传到COS失败: {}", e.getMessage());
            return BaseResponse.error(INTERNAL_SERVER_ERROR);
        }

        // 创建临时文件
        File tempFile;
        try {
            tempFile = File.createTempFile("uploaded-", file.getOriginalFilename());
            file.transferTo(tempFile); // 将 MultipartFile 写入到临时文件
        } catch (IOException e) {
            log.error("临时文件创建失败: {}", e.getMessage());
            return BaseResponse.error(INTERNAL_SERVER_ERROR);
        }

        // 检查输入文件是否存在
        if (!tempFile.exists()) {
            log.error("临时文件不存在: {}", tempFile.getAbsolutePath());
            return BaseResponse.error(NOT_FOUND);
        }

        // 定义输出文件路径
        String outputFilePath = OUTPUT_DIR + file.getOriginalFilename().replaceAll("\\.[^.]+$", "") + "." + format;

        // 创建图像转换命令
        ImageMagickCmd imageMagick = new ImageMagickCmd("magick");
        imageMagick.setSearchPath("D:/ImageMagick-7.1.1-Q16");  // 设置 ImageMagick 的安装路径

        IMOperation operation = new IMOperation();
        operation.addImage(tempFile.getAbsolutePath()); // 添加输入文件
        operation.addImage(outputFilePath); // 添加输出文件

        try {
            // 执行转换
            imageMagick.run(operation);
            log.info("执行的命令是: magick convert {} -> {}", tempFile.getAbsolutePath(), outputFilePath);
            log.info("成功将文件转换为: {}", outputFilePath);
            return BaseResponse.success(outputFilePath);
        } catch (Exception e) {
            log.error("文件转换失败: {}", e.getMessage());
            return BaseResponse.error(INTERNAL_SERVER_ERROR);
        } finally {
            // 处理完成后删除临时文件
            try {
                Files.delete(tempFile.toPath());
            } catch (IOException e) {
                log.warn("临时文件删除失败: {}", e.getMessage());
            }
        }
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
