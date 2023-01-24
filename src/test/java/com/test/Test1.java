package com.test;

import com.lz.mybatis.plugin.utils.SqlParseUtils;
import com.lz.mybatis.plugin.utils.t.Tuple2;
import org.junit.Test;

public class Test1 {

    @Test
    public void test1(){

        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectByIF").getData();
        System.out.println(a.getSecond());
    }


    @Test
    public void test2(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectUserByRealName").getData();
        System.out.println(a.getSecond());
    }

    @Test
    public void test3(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectUserAccountBorrowByLeftJoinOns").getData();
        System.out.println(a.getSecond());
    }

    @Test
    public void test4(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectUserAccountBorrowByFrom").getData();
        System.out.println(a.getSecond());
    }

    @Test
    public void test5(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectUserAccountBorrowByMax").getData();
        System.out.println(a.getSecond());
    }

    @Test
    public void test6(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectUserAccountBorrowByMax1").getData();
        System.out.println(a.getSecond());
    }

    @Test
    public void test7(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectUserAccountByCount").getData();
        System.out.println(a.getSecond());
    }


    @Test
    public void test8(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectPageInfo").getData();
        System.out.println(a.getSecond());
    }



    @Test
    public void test9(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectPageInfoXXX").getData();
        System.out.println(a.getSecond());
    }



    @Test
    public void test10(){
        Tuple2<Boolean,String> a = SqlParseUtils.testUpdate(TestUserMapper.class,"updateCoverTestUserById").getData();
        System.out.println(a.getSecond());
    }


    @Test
    public void test11(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"countByProductIdGroupByUserId").getData();
        System.out.println(a.getSecond());
    }






    @Test
    public void test12(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectPage").getData();
        System.out.println(a.getSecond());
    }



    @Test
    public void test13(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectTestUserByIds").getData();
        System.out.println(a.getSecond());
    }


    @Test
    public void test14(){
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectUserAccountBorrowByLeftJoinOnsNew").getData();
        System.out.println(a.getSecond());
    }


    @Test
    public void test15() {
        Tuple2<Boolean, String> a = SqlParseUtils.testSelect(TestUserMapper.class, "selectUserAccountByCountNew").getData();
        System.out.println(a.getSecond());
    }


    @Test
    public void test16() {
        Tuple2<Boolean, String> a = SqlParseUtils.testSelect(TestUserMapper.class, "selectUserAccountBorrowByFrom").getData();
        System.out.println(a.getSecond());
    }

}



