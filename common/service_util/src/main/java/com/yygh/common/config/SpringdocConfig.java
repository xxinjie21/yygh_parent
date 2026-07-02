package com.yygh.common.config;

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
public class SpringdocConfig {

    @Bean
    public GroupedOpenApi webApiGroup() {
        return GroupedOpenApi.builder()
                .group("webApi")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApiGroup() {
        return GroupedOpenApi.builder()
                .group("adminApi")
                .pathsToMatch("/admin/**")
                .build();
    }

    @Bean
    public OpenAPI webApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("网站-API文档")
                        .description("本文档描述了网站微服务接口定义")
                        .version("1.0")
                        .contact(new Contact().name("yygh").url("http://yygh.com").email("493211102@qq.com")));
    }

    @Bean
    public OpenAPI adminApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("后台管理系统-API文档")
                        .description("本文档描述了后台管理系统微服务接口定义")
                        .version("1.0")
                        .contact(new Contact().name("yygh").url("http://yygh.com").email("49321112@qq.com")));
    }
}
