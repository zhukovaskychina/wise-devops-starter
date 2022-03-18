package com.wiseoutbound.classloader.utils;

import com.wiseoutbound.classloader.loader.DevOpsClassLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ClassLoaderHelper {


    protected static ClassLoaderHelper classLoaderHelper;
    private static boolean isWindowsOs = System.getProperty("os.name").toLowerCase().contains("win");
    protected URL[] classPathUrls;
    //系统classloader
    protected ClassLoader parentClassLoader;
    //自定义classloader
    protected ClassLoader currentClassLoader;


    public ClassLoaderHelper(ClassLoader parentClassLoader, String customerExtDir) {
        this.classPathUrls = buildClassPathUrls(customerExtDir);
        this.parentClassLoader = parentClassLoader;
        this.currentClassLoader = new DevOpsClassLoader(classPathUrls, parentClassLoader);
    }

    public static ClassLoaderHelper getClassLoaderHelper(String customerExtDir) {
        if (classLoaderHelper == null) {
            classLoaderHelper = new ClassLoaderHelper(DevOpsClassLoaderUtils.class.getClassLoader(), customerExtDir);
        }
        return classLoaderHelper;
    }

    public ClassLoader getClassLoader() {
        return currentClassLoader;
    }

    private URL[] buildClassPathUrls(String customerExtDir) {
        List<URL> urlList = new ArrayList();


        //构建系统包
        if (StringUtils.isNotBlank(customerExtDir)) {
            try {
                Collection<File> fileList = FileUtils.listFiles(new File(customerExtDir), new String[]{"jar"}, false);
                for (File file : fileList) {
                    urlList.add(file.toURI().toURL());
                }


            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        String[] classPathArray;
        if (isWindowsOs) {
            classPathArray = System.getProperty("java.class.path").split(File.pathSeparator);
            System.out.println(Arrays.toString(classPathArray));
            for (String classPath : classPathArray) {

                if (classPath.startsWith("./")) {
                    classPath = classPath.substring(2);
                }

                File file = new File(classPath);
                if (file.exists()) {
                    try {
                        urlList.add(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {

            //构建classpath 系统级别
            classPathArray = System.getProperty("java.class.path").split(File.pathSeparator);
            for (String classPath : classPathArray) {

                if (classPath.startsWith("./")) {
                    classPath = classPath.substring(2);
                }

                File file = new File(classPath);
                if (file.exists()) {
                    try {
                        urlList.add(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }


        return urlList.toArray(new URL[urlList.size()]);
    }
}
