package com.lz.mybatis.plugin.annotations;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Froms {

    Item[] value() default {};

}
