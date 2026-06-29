package com.yygh.hospital.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自定义全局异常类
 *
 * @author XXJ
 */
@Data
@Schema(name = "自定义全局异常类")
public class YyghException extends RuntimeException {

    @Schema(description = "异常状态码")
    private Integer code;

    /**
     * 通过状态码和错误消息创建异常对象
     * @param message
     * @param code
     */
    public YyghException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public YyghException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    @Override
    public String toString() {
        return "GuliException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }
}
