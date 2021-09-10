package com.lz.mybatis.plugin.annotations;


import java.lang.annotation.*;

/**
 *  * 乘法操作，主要用于更新操作
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface Mul {
    //数据库中对应的字段
    String value() default "";

}
