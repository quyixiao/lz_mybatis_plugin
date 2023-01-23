package com.test.test;

import com.lz.mybatis.plugin.entity.EntryInfo;
import com.lz.mybatis.plugin.utils.SqlParseUtils;
import com.lz.mybatis.plugin.utils.t.Tuple2;
import jdk.internal.dynalink.support.ClassLoaderGetterContextProvider;
import org.junit.Test;

public class Test2 {

    @Test
    public void test1(){
        SqlParseUtils.getSqlContext().asList.add("zc_1");
        SqlParseUtils.getSqlContext().asList.add("zc_2");
        SqlParseUtils.getSqlContext().asList.add("zc_3");
        SqlParseUtils.getSqlContext().asList.add("zc");

        String entryInfo = SqlParseUtils.getAs("zns_cout");
        System.out.println(entryInfo);

    }

}



