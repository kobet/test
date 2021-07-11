package com.mo.shu.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockProcess {

    /**
     * mock方法
     *
     * @return
     */
    String methodName();

    /**
     * mock类
     *
     * @return
     */
    Class<?> clazz();

    /**
     * mock 实例名
     *
     * @return
     */
    String objName();
}
