//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.lz.mybatis.plugin.plugins.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface TableField {
    String value() default "";

    boolean exist() default true;

    String condition() default "";

    String update() default "";

    FieldStrategy insertStrategy() default FieldStrategy.DEFAULT;

    FieldStrategy updateStrategy() default FieldStrategy.DEFAULT;

    FieldStrategy whereStrategy() default FieldStrategy.DEFAULT;

    FieldFill fill() default FieldFill.DEFAULT;

    boolean select() default true;

    boolean keepGlobalFormat() default false;

    JdbcType jdbcType() default JdbcType.UNDEFINED;

    Class<? extends TypeHandler> typeHandler() default UnknownTypeHandler.class;

    boolean javaType() default false;

    String numericScale() default "";
}
