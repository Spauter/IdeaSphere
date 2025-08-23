package com.spauter.ideasphere.controller;

import com.spauter.ideasphere.entity.Section;
import com.spauter.ideasphere.service.SectionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.spauter.extra.baseentity.utils.ValueUtil.getIntValue;
import static com.spauter.extra.baseentity.utils.ValueUtil.isBlank;


@RestController
@RequestMapping("section")
@Slf4j
public class SectionController {
    @Resource(name = "sectionService")
    private SectionService service;

    @RequestMapping("create")
    public Map<String, Object> createSection(Section section) throws SQLException {
        var map = checkSection(section);
        if (getIntValue(map.get("code")) != 200) {
            return map;
        }
        try {

            service.insertOne(section);
            map.put("code", 200);
            map.put("msg", "创建成功");
            return map;
        } catch (Exception e) {
            log.error(e.getMessage());
            map.put("code", 500);
            map.put("msg", "创建失败");
            return map;
        }
    }

    @RequestMapping("edit/{section_id}")
    public Map<String, Object> editeSection(Section section, @PathVariable String section_id) throws SQLException {
        var originSection = service.findById(section_id);
        var map =checkSection(section);
        if (getIntValue(map.get("code")) != 200) {
            return map;
        }
        section.setId(originSection.getId());
        try {
            service.updateById(section);
            map.put("code", 200);
            map.put("msg", "修改成功");
            return map;
        }catch (Exception e) {
            log.error(e.getMessage());
            map.put("code", 500);
            map.put("msg", "修改失败");
            return map;
        }
    }


    private Map<String, Object> checkSection(Section section) throws SQLException {
        var map = new HashMap<String, Object>();
        if (section == null) {
            map.put("code", 400);
            map.put("msg", "参数不能为空");
            return map;
        }
        if (isBlank(section.getName())) {
            map.put("code", 400);
            map.put("msg", "名称不能为空");
            return map;
        }
        if (isBlank(section.getDescription())) {
            map.put("code", 400);
            map.put("msg", "描述不能为空");
            return map;
        }
        if (!service.checkSectionExisisByName(section.getName())) {
            map.put("code", 400);
            map.put("msg", "该板块已存在");
            return map;
        } else {
            map.put("code", 200);
            return map;
        }
    }
}
