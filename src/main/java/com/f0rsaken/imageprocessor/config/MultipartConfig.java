package com.f0rsaken.imageprocessor.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.parse("124MB")); // 设置单个文件的最大大小
        factory.setMaxRequestSize(DataSize.parse("124MB")); // 设置请求中所有文件的最大大小
        return factory.createMultipartConfig(); // 返回配置对象
    }
}
