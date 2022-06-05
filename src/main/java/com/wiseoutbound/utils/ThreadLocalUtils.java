package com.wiseoutbound.utils;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ThreadLocalUtils {


    private static final ThreadLocal<Properties> threadLocal = new ThreadLocal<>();


    private static final ThreadLocal<Map<String,String>> threadLocalHotSwapBeanMap=new ThreadLocal<>();

    private static final ThreadLocal<List<String>> threadLocalDisabledBeanList=new ThreadLocal<>();


    public static Properties getDevopsProperties() {
        return threadLocal.get();
    }

    public static void setDevopsProperties(Properties properties) {
        threadLocal.set(properties);
    }

    public static Map<String, String> getThreadLocalHotSwapBeanMap() {
        return threadLocalHotSwapBeanMap.get();
    }

    public static ThreadLocal<List<String>> getThreadLocalDisabledBeanList() {
        return threadLocalDisabledBeanList;
    }
}
