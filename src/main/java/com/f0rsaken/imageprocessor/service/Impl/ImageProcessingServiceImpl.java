package com.f0rsaken.imageprocessor.service.Impl;

import com.f0rsaken.imageprocessor.common.BaseResponse;
import com.f0rsaken.imageprocessor.common.ResponseCode;
import com.f0rsaken.imageprocessor.service.ImageProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.im4java.core.IMOperation;
import org.im4java.core.ImageMagickCmd;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
public class ImageProcessingServiceImpl implements ImageProcessingService {

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
        // 创建临时文件
        File tempFile;
        try {
            tempFile = File.createTempFile("uploaded-", file.getOriginalFilename());
            file.transferTo(tempFile); // 将 MultipartFile 写入到临时文件
        } catch (IOException e) {
            log.error("临时文件创建失败: {}", e.getMessage());
            return BaseResponse.error(ResponseCode.INTERNAL_SERVER_ERROR);
        }

        // 检查输入文件是否存在
        if (!tempFile.exists()) {
            log.error("临时文件不存在: {}", tempFile.getAbsolutePath());
            return BaseResponse.error(ResponseCode.NOT_FOUND);
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
            return BaseResponse.error(ResponseCode.INTERNAL_SERVER_ERROR);
        } finally {
            // 处理完成后删除临时文件
            try {
                Files.delete(tempFile.toPath());
            } catch (IOException e) {
                log.warn("临时文件删除失败: {}", e.getMessage());
            }
        }
    }
}
