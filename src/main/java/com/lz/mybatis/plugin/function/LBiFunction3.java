package com.lz.mybatis.plugin.function;


import java.io.Serializable;

@FunctionalInterface
public interface LBiFunction3<T1, T2, T3, T4, R> extends Serializable {

    /**
     *
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4);
}