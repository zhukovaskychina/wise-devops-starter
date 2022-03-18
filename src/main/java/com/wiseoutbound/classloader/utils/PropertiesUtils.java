package com.wiseoutbound.classloader.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PropertiesUtils {
    /***
     *
     * 读取指定jar包当中的文件
     * @param  jar  指定jar包
     * @param propertyFileName 属性文件名称
     * ****/
    public static Properties readPropertiesFromJar(JarFile jar, String propertyFileName) throws IOException {
        Enumeration<JarEntry> en = jar.entries();
        Properties props = new Properties();
        while (en.hasMoreElements()) {
            JarEntry je = en.nextElement();
            String name = je.getName();
            if (StringUtils.equals(name, propertyFileName)) {
                InputStream input = jar.getInputStream(je);
                props.load(input);
                input.close();
            }

        }
        return props;
    }
}
