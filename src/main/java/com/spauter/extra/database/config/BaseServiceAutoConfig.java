package com.spauter.extra.database.config;

import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.database.service.BaseService;
import com.spauter.extra.database.service.impl.BaseServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BaseServiceAutoConfig {


    /**
     * 或者使用更灵活的方式：不指定具体实体类
     * 在实际使用时动态确定实体类型
     */
    @Bean
    public BaseService<?> genericBaseService() {
        // 返回一个可以适配多种实体类型的BaseService
        return new GenericBaseService();
    }

}

/**
 * 通用的BaseService实现，只能使用
 * {@link BaseServiceImpl#selectByPage(String, int, int, Object...)}
 * {@link BaseServiceImpl#selectBySql(String, Object...)}
 * {@link  BaseServiceImpl#selectList(String, Object, Object...)}
 * {@link BaseServiceImpl#selectOne(String, Object, Object...)} }
 */
class GenericBaseService extends BaseServiceImpl<Object> implements BaseService<Object> {

}