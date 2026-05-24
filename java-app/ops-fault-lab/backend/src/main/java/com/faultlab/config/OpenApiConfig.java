package com.faultlab.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档便于运维同学用 Swagger 和 Apifox 快速联调接口。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Ops Fault Lab API")
                .description("运维故障演练平台后端接口")
                .version("1.0.0")
                .contact(new Contact().name("fault-lab").email("ops@example.com")));
    }
}
