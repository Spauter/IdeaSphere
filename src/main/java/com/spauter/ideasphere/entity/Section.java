package com.spauter.ideasphere.entity;

import com.spauter.extra.database.annotations.TableId;
import com.spauter.extra.baseentity.enums.IdType;
import lombok.Data;

@Data
public class Section {
    @TableId(idType = IdType.AUTO_INCREMENT)
    private Integer id;
    

    private String name;
    

    private String description;
    

    private String icon;
    

    private Integer postCount;
    

    private Integer commentCount;
}