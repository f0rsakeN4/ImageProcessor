package com.f0rsaken.imageprocessor.controller;

import com.f0rsaken.imageprocessor.common.BaseResponse;
import com.f0rsaken.imageprocessor.common.ResponseCode;
import com.f0rsaken.imageprocessor.exception.FileUploadException;
import com.f0rsaken.imageprocessor.service.ImageProcessingService;
import com.f0rsaken.imageprocessor.service.ImageUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpSession;

import static com.f0rsaken.imageprocessor.common.ResponseCode.*;

@RestController
@RequestMapping("/image")
@Api(tags = "图像处理相关")
public class ImageController {

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private ImageProcessingService imageProcessingService;

    @PostMapping("/upload")
    @ApiOperation("上传文件到本地")
    public BaseResponse<String> uploadFileToLocal(@RequestParam("file") MultipartFile file, HttpSession session) {
        // 调用服务层的方法进行文件上传
        try {
            // 调用服务层方法
            // 上传文件并获取文件路径
            String fileName = imageUploadService.uploadToLocal(file);
            if (fileName != null) {
                // 将文件路径存入会话中
                session.setAttribute("uploadedFileName", fileName);
                return BaseResponse.success(fileName);
            }
            return BaseResponse.error(FILE_UPLOAD_ERROR);
        } catch (FileUploadException e) {
            // 处理文件上传特定的异常
            return BaseResponse.error(e.getResponseCode(), e.getMessage());
        } catch (Exception e) {
            // 处理其他未预见的异常
            return BaseResponse.error(INTERNAL_SERVER_ERROR, "未定义异常");
        }
    }

    @PostMapping("/convert")
    @ApiOperation("转换文件格式")
    public BaseResponse<String> convertImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam String format) {
        // 调用服务层的方法进行图片格式转换
        return imageProcessingService.convertImageFormat(file, format);
    }
}
