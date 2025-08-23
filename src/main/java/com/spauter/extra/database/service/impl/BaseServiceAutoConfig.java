package com.spauter.extra.database.service.impl;

import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.database.annotations.TableId;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BaseServiceAutoConfig {

    private class AUselessEntity{
        int a;
        int b;
        String name;
        @TableId
        String id;
    }

    @Bean
    public BaseServiceImpl<?> baseServiceImpl(){
        ClassFieldSearcher searcher=new ClassFieldSearcher(AUselessEntity.class);
        return new BaseServiceImpl<>(searcher);
    }
}
