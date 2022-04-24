package com.lz.mybatis.plugin.annotations;


import java.lang.annotation.*;


/**
 * 排序第4位
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MapF2F {

    String key() default "id";

    String value() default "this";

}
