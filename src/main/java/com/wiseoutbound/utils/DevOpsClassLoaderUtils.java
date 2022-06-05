package com.wiseoutbound.utils;

public class DevOpsClassLoaderUtils {


    public static ClassLoader getClassLoader(String dir) {
        return ClassLoaderHelper.getClassLoaderHelper(dir).getClassLoader();
    }
}
