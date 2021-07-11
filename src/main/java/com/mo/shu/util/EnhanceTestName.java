package com.mo.shu.util;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class EnhanceTestName extends TestWatcher {

    /**
     * 用例方法
     */
    private String className;

    /**
     * 用例属性类
     */
    private Class<?> clazz;

    /**
     * Invoked when a test is about to start
     *
     * @param description
     */
    @Override
    protected void starting(Description description) {
        className = description.getClassName();
        clazz = description.getTestClass();
    }

    public String getClassName() {
        return className;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
