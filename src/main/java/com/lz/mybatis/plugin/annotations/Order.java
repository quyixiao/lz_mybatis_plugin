package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;


/**
 * 排序第三位
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Order {
    By[] value() default {};
}