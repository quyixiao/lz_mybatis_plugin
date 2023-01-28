package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;

/**
 * LE 就是 LESS THAN   小于等于
 *
 * <=
 *
 *
 */
// gmt_create  <![CDATA[ <= ]]>  #{startTimeLE}
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface LE {
    String value() default "";
}
