package com.lz.mybatis.plugin.entity;

import com.lz.mybatis.plugin.utils.SqlParseUtils;

public class TableBaseInfo {

    private String id;
    private String isDelete;
    private String gmtCreate;
    private String gmtModified;



    public TableBaseInfo() {


    }

    public TableBaseInfo(String id, String isDelete, String gmtCreate, String gmtModified) {
        this.id = id;
        this.isDelete = isDelete;
        this.gmtCreate = gmtCreate;
        this.gmtModified = gmtModified;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
    }

    public String getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(String gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(String gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getJavaCodeId() {
        return SqlParseUtils.colomn2JavaCode(this.id);
    }

    public String getJavaCodeIsDelete() {
        return SqlParseUtils.colomn2JavaCode(this.isDelete);
    }

    public String getJavaCodeGmtCreate() {
        return SqlParseUtils.colomn2JavaCode(this.gmtCreate);
    }

    public String getJavaCodeGmtModified() {
        return SqlParseUtils.colomn2JavaCode(this.gmtModified);
    }
}
