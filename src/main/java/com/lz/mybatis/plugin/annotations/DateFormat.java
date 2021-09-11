package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface DateFormat {
    String value() default "%Y-%m-%d %H:%i:%S";
}

