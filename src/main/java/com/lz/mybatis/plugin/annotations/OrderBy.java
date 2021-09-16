package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;

/**
 * 在方法参数中排序是第一位。
 * 在方法上注解的排序是第二位
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
public @interface OrderBy {
    String[] value() default {};
}
