package com.example.eStore.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dlwf1izos",
                "api_key", "477756124483842",
                "api_secret", "hQBSLb7R2euWcBvsx_juj6aKaSI"
        ));
    }
}
