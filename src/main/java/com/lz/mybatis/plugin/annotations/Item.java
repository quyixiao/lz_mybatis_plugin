package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Item {

    Class<?>[] value() default {};

    String as() default "";

    String on() default "";

    String left() default "";

    String right() default "";

}
