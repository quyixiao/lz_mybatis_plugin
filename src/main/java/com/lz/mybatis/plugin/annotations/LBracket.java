package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.TYPE,ElementType.FIELD})
public @interface LBracket {

    String value() default "";

}






