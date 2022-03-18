package com.wiseoutbound.classloader.utils;

import java.util.Properties;

public class ThreadLocalUtils {


    private static final ThreadLocal<Properties> threadLocal = new ThreadLocal<>();

    public static Properties getDevopsProperties() {
        return threadLocal.get();
    }

    public static void setDevopsProperties(Properties properties) {
        threadLocal.set(properties);
    }
}
