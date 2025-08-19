package com.spauter.extra.systeminit.config;

import com.spauter.extra.systeminit.intercept.SuperAdminRegister;
import com.spauter.extra.systeminit.intercept.SystemInitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * <h3><b>拦截器注册类</b></h3>
 * 该类主要是用来注册拦截器的，并且会按照{@link #addInterceptors(InterceptorRegistry)}里面
 * {@code registry.addInterceptor(MyInterceptor).addPathPatterns("/**");}的顺序执行拦截器
 *<p>这里首先通过{@code SuperAdminRegister}注册一个默认的超级管理员，用于{@code SystemInitInterceptor}的超级管理员认证</p>
 * @author spauter
 */
@Configuration
public class SystemAdapter implements WebMvcConfigurer {

	@Resource
	private SuperAdminRegister superAdminRegister;
	@Resource
	private SystemInitInterceptor systemInitInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(superAdminRegister).addPathPatterns("/system/**");
		registry.addInterceptor(systemInitInterceptor).addPathPatterns("/system/**");
	}
}
