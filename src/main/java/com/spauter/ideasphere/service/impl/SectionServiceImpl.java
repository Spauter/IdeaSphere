package com.spauter.ideasphere.service.impl;

import com.spauter.extra.database.service.impl.BaseServiceImpl;
import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.ideasphere.entity.Section;
import com.spauter.ideasphere.service.SectionService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service("sectionService")
public class SectionServiceImpl extends BaseServiceImpl<Section> implements SectionService {

    @Override
    public boolean checkSectionExisisByName(String sectionName) throws SQLException {
        QueryWrapper<Section> queryWrapper = new QueryWrapper<>();
        queryWrapper.addEq("name", sectionName);
        return findOne(queryWrapper) != null;
    }
}
