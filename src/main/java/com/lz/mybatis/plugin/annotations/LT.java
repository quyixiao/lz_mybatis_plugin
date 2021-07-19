package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;


/***
 *
 * LT 就是 LESS THAN OR EQUAL 小于
 *
 *
 * <
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface LT {
    String value() default "";
}
