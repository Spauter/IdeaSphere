package com.spauter.extra.config.init;

import org.ideasphere.ideasphere.Config.ApplicationConfig;
import org.springframework.stereotype.Service;
/*
这样做是让Spring把ConfigChecker注入到springIOC容器中
 */
@Service
public class ApplicationCheck extends ApplicationConfig {
}
