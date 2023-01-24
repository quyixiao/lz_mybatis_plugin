package com.test;

import com.lz.mybatis.plugin.utils.TestParseUtils;
import com.lz.mybatis.plugin.utils.t.Tuple2;
import org.junit.Test;

public class Test2 {


    @Test
    public void test5() {
        Tuple2<Boolean, String> a = TestParseUtils.testSql(TestUserMapper::updateUserAmountCondition).getData();
        System.out.println(a.getSecond());
    }


    @Test
    public void test6() {
        Tuple2<Boolean, String> a = TestParseUtils.testSql(TestUserMapper::selectUserByRealNameObject1).getData();
        System.out.println(a.getSecond());
    }


    @Test
    public void test7() {
        Tuple2<Boolean, String> a = TestParseUtils.testSql(TestUserMapper::selectUserByRealNameObject2).getData();
        System.out.println(a.getSecond());
    }



    @Test
    public void test3() {
        Tuple2<Boolean, String> a = TestParseUtils.testSql(TestUserMapper::selectUserByRealNameObject3).getData();
        System.out.println(a.getSecond());
    }


}



