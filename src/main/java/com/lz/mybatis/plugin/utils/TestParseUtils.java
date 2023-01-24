package com.lz.mybatis.plugin.utils;

import com.lz.mybatis.plugin.function.*;
import com.lz.mybatis.plugin.utils.t.PluginTuple;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

public class TestParseUtils {

    /**
     * 没有参数sql 测试
     */
    public static <T> PluginTuple testSql(LBiFunction0<T, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //  1 个参数的sql 解析
    public static <T, U> PluginTuple testSql(LBiFunction1<T, U, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2 个参数的sql测试
    public static <T1, T2, T3> PluginTuple testSql(LBiFunction2<T1, T2, T3, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3 个参数的sql测试
    public static <T1, T2, T3, T4> PluginTuple testSql(LBiFunction3<T1, T2, T3, T4, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 4 个参数的sql测试
    public static <T1, T2, T3, T4, T5> PluginTuple testSql(LBiFunction4<T1, T2, T3, T4, T5, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 5 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6> PluginTuple testSql(LBiFunction5<T1, T2, T3, T4, T5, T6, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 6 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7> PluginTuple testSql(LBiFunction6<T1, T2, T3, T4, T5, T6, T7, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 7 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8> PluginTuple testSql(LBiFunction7<T1, T2, T3, T4, T5, T6, T7, T8, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 8 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> PluginTuple testSql(LBiFunction8<T1, T2, T3, T4, T5, T6, T7, T8, T9, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 9 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> PluginTuple testSql(LBiFunction9<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 10 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> PluginTuple testSql(LBiFunction10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 11 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> PluginTuple testSql(LBiFunction11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 12 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> PluginTuple testSql(LBiFunction12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 13 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> PluginTuple
    testSql(LBiFunction13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 14 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> PluginTuple
    testSql(LBiFunction14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 15 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> PluginTuple
    testSql(LBiFunction15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 16 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> PluginTuple
    testSql(LBiFunction16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 17 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> PluginTuple
    testSql(LBiFunction17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 18 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> PluginTuple
    testSql(LBiFunction18<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 19 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> PluginTuple
    testSql(LBiFunction19<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 20 个参数的sql测试
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21> PluginTuple
    testSql(LBiFunction20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, ?> function) {
        Method method = function.getClass().getDeclaredMethods()[1];
        method.setAccessible(true);
        try {
            SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) method.invoke(function);
            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace("/", ".");
            Class clazz = Class.forName(className);
            return SqlParseUtils.testSql(clazz, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
