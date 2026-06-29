package com.yygh.hospital.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI 配置 (Swagger3)
 *
 * @author XXJ
 */
@Configuration
public class Swagger2Config {

    @Bean
    public GroupedOpenApi webApiGroup() {
        return GroupedOpenApi.builder()
                .group("webApi")
                .pathsToMatch("/P2P/**")
                .build();
    }

    @Bean
    public OpenAPI webApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("网站-API文档")
                        .description("本文档描述了网站微服务接口定义")
                        .version("1.0")
                        .contact(new Contact().name("qy").url("http://yygh.com").email("55317332@qq.com")));
    }
}
