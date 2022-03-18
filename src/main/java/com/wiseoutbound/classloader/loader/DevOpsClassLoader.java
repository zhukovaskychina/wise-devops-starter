package com.wiseoutbound.classloader.loader;

import java.net.URL;
import java.net.URLClassLoader;


public class DevOpsClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    public DevOpsClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        return super.findClass(name);
    }
}
