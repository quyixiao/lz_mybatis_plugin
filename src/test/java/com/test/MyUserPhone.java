package com.test;

import com.lz.mybatis.plugin.annotations.*;

import java.util.Date;


public class MyUserPhone implements java.io.Serializable {
    //主键，自增id
    private Long id;
    //是否删除状态，1：删除，0：有效
    private Integer isDelete;
    //创建时间
    @DateFormat("%Y-%m-%d")
    private Date gmtCreate;
    //最后修改时间
    private Date gmtModified;
    //用户名
    @IF
    private String userNameEn;
    //姓名
    @IF
    private String realNameEn;
    //身份证号
    @IF
    private String idNumber;
    //身份证号
    @IF
    private String idNumberEn;
    //登录失败次数
    @IsNotNull
    private Integer failCount;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getUserNameEn() {
        return userNameEn;
    }

    public void setUserNameEn(String userNameEn) {
        this.userNameEn = userNameEn;
    }

    public String getRealNameEn() {
        return realNameEn;
    }

    public void setRealNameEn(String realNameEn) {
        this.realNameEn = realNameEn;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getIdNumberEn() {
        return idNumberEn;
    }

    public void setIdNumberEn(String idNumberEn) {
        this.idNumberEn = idNumberEn;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }
}