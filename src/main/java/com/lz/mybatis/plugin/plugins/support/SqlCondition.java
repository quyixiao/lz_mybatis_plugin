//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.lz.mybatis.plugin.plugins.support;

public class SqlCondition {
    public static final String EQUAL = "%s=#{%s}";
    public static final String NOT_EQUAL = "%s&lt;&gt;#{%s}";
    public static final String LIKE = "%s LIKE CONCAT('%%',#{%s},'%%')";
    public static final String LIKE_LEFT = "%s LIKE CONCAT('%%',#{%s})";
    public static final String LIKE_RIGHT = "%s LIKE CONCAT(#{%s},'%%')";

    public SqlCondition() {
    }
}
