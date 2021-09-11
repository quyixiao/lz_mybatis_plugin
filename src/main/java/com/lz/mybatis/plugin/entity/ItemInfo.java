package com.lz.mybatis.plugin.entity;

public class ItemInfo {

    private Class clazz;
    private String as;
    private String on;

    public ItemInfo() {
    }

    public ItemInfo(Class clazz, String as, String on) {
        this.clazz = clazz;
        this.as = as;
        this.on = on;
    }

    public ItemInfo(Class clazz, String as) {
        this.clazz = clazz;
        this.as = as;
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
