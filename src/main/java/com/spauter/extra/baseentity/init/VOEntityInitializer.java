package com.spauter.extra.baseentity.init;

import com.spauter.extra.baseentity.annotations.VOEntityScan;
import com.spauter.extra.baseentity.enums.RelationType;
import com.spauter.extra.baseentity.searcher.RelationColumns;
import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.baseentity.utils.ValueUtil;
import com.spauter.extra.database.annotations.VORelation;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * 用于扫描实体并注册到{@link RelationColumns}中
 */
@Configuration
public class VOEntityInitializer {
    private Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }


    @PostConstruct
    public void searchVO() {
        Class<?> mainClass = deduceMainApplicationClass();
        if (mainClass == null) {
            return;
        }
        VOEntityScan scan = mainClass.getAnnotation(VOEntityScan.class);
        String[] scanBasePackages;
        Class<?>[] scanClasses;
        if(scan==null){
            scanClasses=new Class[]{};
            scanBasePackages=new String[]{};
        }else {
            scanBasePackages = scan.scanBasePackages();
            scanClasses = scan.scanClasses();
        }
        if (ValueUtil.isBlank(scanBasePackages)) {
            scanBasePackages = new String[]{"com.spauter.ideasphere.entity"};
        }
        SimpleClassScan simpleClassScan = new SimpleClassScan();
        Set<Class<?>> set = simpleClassScan.scan(scanBasePackages);
        set.addAll(List.of(scanClasses));
        for (Class<?> clazz : set) {
            ClassFieldSearcher.addSearcher(clazz);
        }
    }
}
