package com.test;

import com.lz.mybatis.plugin.utils.TestParseUtils;
import com.lz.mybatis.plugin.utils.t.Tuple2;
import org.junit.Test;

public class Test2 {


    @Test
    public void test5() {
        Tuple2<Boolean, String> a = TestParseUtils.testSql(TestUserMapper::selectUserAccountBorrowByFrom).getData();
        System.out.println(a.getSecond());
    }





}



