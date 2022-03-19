package com.wiseoutbound.abstractHandler;

import com.wiseoutbound.classloader.utils.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.Properties;

import static com.wiseoutbound.consts.SystemParams.ENABLE_FILTER;
import static com.wiseoutbound.consts.SystemParams.FILTER_SCAN_PACKAGE;


@Slf4j
public class CustomerRestControllerFilter implements TypeFilter {

    Properties properties;
    private Boolean enableScanFilterPackage;
    private String[] scanPackageList;

    public CustomerRestControllerFilter(){
        log.warn("你已经开启了定制化restController模式，这意味着你可以在定制化库中编写和原来一模一样的接口名，但是具体实现内容不同的代码");
        init();
    }

    private void init() {
        this.properties=ThreadLocalUtils.getDevopsProperties();
        String enableFilter=properties.getProperty(ENABLE_FILTER);
        this.enableScanFilterPackage=Boolean.parseBoolean(enableFilter);
        String filterScanPackage=properties.getProperty(FILTER_SCAN_PACKAGE);
        String[] scanPackages=StringUtils.split(filterScanPackage,";");
        if (scanPackages==null){
            scanPackages=new String[0];
        }
        this.scanPackageList=scanPackages;
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        if (enableScanFilterPackage){
            String className=metadataReader.getClassMetadata().getClassName();

            boolean isTarget=false;
            for (String iter:this.scanPackageList) {
                if (!StringUtils.startsWith(className,iter)){
                    continue;
                }
                isTarget=true;
            }
            return isTarget;
        }
        return false;
    }
}
