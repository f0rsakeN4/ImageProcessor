package com.f0rsaken.imageprocessor.controller;

import com.f0rsaken.imageprocessor.common.BaseResponse;
import com.f0rsaken.imageprocessor.service.ImageProcessingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/image")
@Api(tags = "图像处理相关")
public class ImageController {

    @Autowired
    private ImageProcessingService imageProcessingService;

    @PostMapping("/convert")
    @ApiOperation("转换文件格式")
    public BaseResponse<String> convertImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam String format) {
        // 调用服务层的方法进行图片格式转换
        return imageProcessingService.convertImageFormat(file, format);
    }
}
