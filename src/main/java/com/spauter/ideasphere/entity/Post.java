package com.spauter.ideasphere.entity;

import com.spauter.extra.database.annotations.TableFiled;
import com.spauter.extra.database.annotations.TableId;
import com.spauter.extra.baseentity.enums.IdType;
import com.spauter.extra.database.annotations.VORelation;
import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;

@Data
public class Post {
    @TableId(idType = IdType.AUTO_INCREMENT)
    private Integer id;


    private String title;


    private String content;


    private String htmlContent;


    private Integer authorId;

    private Boolean deleted = false;


    private String deleteReason;


    private LocalDateTime deleteTime;


    private Integer likeCount;


    private Integer lookCount;


    private Integer sectionId;

    private LocalDateTime createdAt;

    @TableFiled(exists = false)
    @VORelation(query = "authorId")
    private User author;

    @TableFiled(exists = false)
    @VORelation(query = "sectionId")
    private Section section;
}