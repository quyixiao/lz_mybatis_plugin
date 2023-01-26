package com.lz.mybatis.plugin.function;


import java.io.Serializable;

@FunctionalInterface
public interface LBiFunction1<T1, T2, R> extends Serializable {

    /**
     *
     */
    R apply(T1 t1, T2 t2);
}