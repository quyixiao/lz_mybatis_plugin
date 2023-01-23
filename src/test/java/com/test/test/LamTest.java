package com.test.test;

import com.lz.mybatis.plugin.plugins.support.LambdaMeta;
import com.lz.mybatis.plugin.plugins.LambdaUtils;

public class LamTest {

    public static void main(String[] args) {
        LambdaMeta lambdaMeta = LambdaUtils.extract(TestUser::getBranchId);
        System.out.println(lambdaMeta);
        System.out.println(lambdaMeta.getInstantiatedClass());
        System.out.println(lambdaMeta.getImplMethodName());
    }
}
