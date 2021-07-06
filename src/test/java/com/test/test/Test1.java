package com.test.test;

import com.lz.mybatis.plugin.utils.SqlParseUtils;
import com.lz.mybatis.plugin.utils.t.Tuple2;

public class Test1 {

    public static void main(String[] args) {
        Tuple2<Boolean,String> a = SqlParseUtils.testSelect(TestUserMapper.class,"selectActGoldCoinByActAccountTypeStatusList").getData();
        System.out.println(a.getSecond());

    }
}
