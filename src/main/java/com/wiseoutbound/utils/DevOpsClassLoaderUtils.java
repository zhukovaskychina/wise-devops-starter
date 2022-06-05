package com.wiseoutbound.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DevOpsClassLoaderUtils {


    public static ClassLoader getClassLoader(String dir, String skippedJarName) {

        URLClassLoader classLoader = (URLClassLoader) DevOpsClassLoaderUtils.class.getClassLoader();

        List<URL> urlList = new ArrayList<>();

        if (StringUtils.isNotBlank(dir)) {
            try {
                List<String> list = getSkipJarList(skippedJarName);

                Collection<File> fileList = FileUtils.listFiles(new File(dir), new String[]{"jar"}, true);

                for (File file : fileList) {
                    if (isInSkippedList(file.getName(), list)) {
                        continue;
                    }
                    urlList.add(file.toURI().toURL());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        try {
            Method add = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            add.setAccessible(true);
            for (int i = 0; i < urlList.size(); i++) {
                add.invoke(classLoader, urlList.get(i).toURI().toURL());
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        return classLoader;
    }

    public static boolean isInSkippedList(String currentJarFileName, List<String> skippedList) {

        for (String jarName : skippedList) {
            if (StringUtils.equals(currentJarFileName, jarName)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getSkipJarList(String skippedJarName) {
        if (StringUtils.isEmpty(skippedJarName)) {
            return new ArrayList<>();
        }
        return Arrays.stream(StringUtils.split(skippedJarName, ";")).collect(Collectors.toList());
    }


}
