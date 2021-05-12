package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;

/**
 * NE 就是 NOT EQUAL 不等于
 * !=
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface NE {
    String value() default "";
}
