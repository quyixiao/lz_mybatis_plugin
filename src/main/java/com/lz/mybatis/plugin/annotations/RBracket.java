package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;


/***
 * 右括号
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.TYPE,ElementType.FIELD})
public @interface RBracket {

    String value() default "";

}






