package com.f0rsaken.imageprocessor.service.Impl;

import com.f0rsaken.imageprocessor.common.BaseResponse;
import com.f0rsaken.imageprocessor.common.ResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageProcessingServiceImplTest {

    @InjectMocks
    private ImageProcessingServiceImpl imageProcessingService;

    private final String inputFilePath = "src/test/resources/InputFile/";
    private final String outputFilePath = "src/test/resources/OutputFile/"; // 设置输出文件路径
    private final String[] formats = {"bmp", "gif", "jpg", "jpeg", "png", "tiff", "webp"};

    @BeforeEach
    public void setUp() {
        // 清空输出文件夹，以便测试
        File outputDir = new File(outputFilePath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        } else {
            for (File file : outputDir.listFiles()) {
                file.delete();
            }
        }
    }

    @Test
    public void testConvertImageFormatSuccess() throws IOException {
        for (String inputFormat : formats) {
            for (String outputFormat : formats) {
                // Create a MockMultipartFile for each input format
                File inputFile = new File(inputFilePath + "BackGround." + inputFormat);
                if (!inputFile.exists()) {
                    continue; // Skip if the input file does not exist
                }

                MultipartFile mockFile = new MockMultipartFile(
                        "file",
                        inputFile.getName(),
                        "image/" + inputFormat,
                        Files.readAllBytes(inputFile.toPath())
                );

                // When
                BaseResponse<String> response = imageProcessingService.convertImageFormat(mockFile, outputFormat);

                // Then
                assertEquals(ResponseCode.SUCCESS.getCode(), response.getCode());
                assertTrue(response.getData().endsWith("." + outputFormat));
            }
        }
    }
}
