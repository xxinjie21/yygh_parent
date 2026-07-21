package com.yygh.common.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean属性拷贝工具类
 *
 * @author XXJ
 */
public class BeanCopyUtils {

    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) return null;
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean copy failed", e);
        }
    }

    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null) return List.of();
        return sourceList.stream()
                .map(s -> copy(s, targetClass))
                .collect(Collectors.toList());
    }
}
