package com.spauter.ideasphere.service;

import com.spauter.extra.database.service.BaseService;
import com.spauter.ideasphere.entity.Section;

import java.sql.SQLException;

public interface SectionService extends BaseService<Section> {
boolean checkSectionExisisByName(String sectionName) throws SQLException;

}
