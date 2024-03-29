package com.lz.mybatis.plugin.annotations;


import java.lang.annotation.*;

/**
 *  /  除法操作，主要用于更新操作
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface Div {

    //数据库中对应的字段
    String value() default "";
}
