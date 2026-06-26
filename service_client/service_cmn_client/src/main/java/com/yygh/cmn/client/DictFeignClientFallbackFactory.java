package com.yygh.cmn.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 数据字典Feign客户端熔断降级工厂
 *
 * @author XXJ
 */
@Slf4j
@Component
public class DictFeignClientFallbackFactory implements FallbackFactory<DictFeignClient> {

    @Override
    public DictFeignClient create(Throwable cause) {
        log.error("DictFeignClient 调用失败，触发熔断降级", cause);
        return new DictFeignClient() {
            @Override
            public String getName(String dictCode, String value) {
                return "";
            }

            @Override
            public String getName(String value) {
                return "";
            }
        };
    }
}
