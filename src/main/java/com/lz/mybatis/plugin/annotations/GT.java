package com.lz.mybatis.plugin.annotations;

import java.lang.annotation.*;

/**
 * 就是 GREATER THAN   大于　
 *
 * >
 */
//  gmt_create  <![CDATA[ > ]]> #{endTimeGT}
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface GT {
    String value() default "";
}
