package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;


/**
 * 就是 GREATER THAN OR EQUAL     大于等于
 * >=
 *
 *
 *
 */
//gmt_create  <![CDATA[ >=]]> #{startTimeGE}
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface GE {
    String value() default "";
}
