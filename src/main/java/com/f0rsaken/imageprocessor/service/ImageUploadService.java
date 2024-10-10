package com.f0rsaken.imageprocessor.service;

import com.f0rsaken.imageprocessor.common.BaseResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    String uploadToLocal(MultipartFile file);
}
