package com.lz.mybatis.plugin.annotations;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Mapping {
    String[] value() default "";

    String[] as() default {};

    MK[] mk() default {};

}
