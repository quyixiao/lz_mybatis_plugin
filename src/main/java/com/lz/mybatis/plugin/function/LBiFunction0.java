package com.lz.mybatis.plugin.function;


import java.io.Serializable;

@FunctionalInterface
public interface LBiFunction0<T1, R> extends Serializable {

    R apply(T1 t1);
}