package com.campus.cycle.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置：启动后访问 /swagger-ui.html 查看接口文档
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campusCycleOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("校园循环 API")
                .description("校园二手交易平台后端接口文档")
                .version("v0.1.0"));
    }
}
