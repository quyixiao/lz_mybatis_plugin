package com.lz.mybatis.plugin.entity;


public class EntryInfo {
    private String tableName;
    private String as ;
    private Class clazz;


    public EntryInfo() {

    }




    public EntryInfo(String tableName, String as, Class clazz) {
        this.tableName = tableName;
        this.as = as;
        this.clazz = clazz;
    }


    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getAs() {
        return as;
    }

    public void setAs(String as) {
        this.as = as;
    }
}
