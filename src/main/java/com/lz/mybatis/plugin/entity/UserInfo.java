package com.lz.mybatis.plugin.entity;

import com.lz.mybatis.plugin.annotations.LBracket;
import com.lz.mybatis.plugin.annotations.RBracket;

public class UserInfo {

    @LBracket
    private Integer age ;
    @RBracket
    private String userName;
    private Integer height;


    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
