package com.lz.mybatis.plugin.entity;

public class ItemInfo {

    private Class clazz;
    private String as;
    private String on;
    private String tableName;

    public ItemInfo() {
    }

    public ItemInfo(Class clazz, String as, String on,String tableName) {
        this.clazz = clazz;
        this.as = as;
        this.on = on;
        this.tableName = tableName;
    }

    public ItemInfo(Class clazz, String as) {
        this.clazz = clazz;
        this.as = as;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getAs() {
        return as;
    }

    public void setAs(String as) {
        this.as = as;
    }

    public String getOn() {
        return on;
    }

    public void setOn(String on) {
        this.on = on;
    }
}
