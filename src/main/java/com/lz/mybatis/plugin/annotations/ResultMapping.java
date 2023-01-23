package com.lz.mybatis.plugin.annotations;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})

public @interface ResultMapping {
    String value() default "";

    Sum[] sum() default {};

    Count[] count() default {};

    Max[] max() default {};

    Min[] min() default {};

    Avg [] avg() default {};
}
