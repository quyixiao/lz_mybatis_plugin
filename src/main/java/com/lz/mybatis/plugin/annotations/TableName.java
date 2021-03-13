package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TableName {
    String value() default "";

    String schema() default "";

    boolean keepGlobalPrefix() default false;

    String resultMap() default "";

    boolean autoResultMap() default false;

    String[] excludeProperty() default {};
}
