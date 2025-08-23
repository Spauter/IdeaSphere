package com.spauter.ideasphere.entity;

import com.spauter.extra.baseentity.enums.IdType;
import com.spauter.extra.baseentity.enums.RelationType;
import com.spauter.extra.database.annotations.TableId;
import com.spauter.extra.database.annotations.VORelation;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    @TableId(idType = IdType.AUTO_INCREMENT)
    private Integer id;
    

    private String content;
    

    private String htmlContent;
    

    private Integer authorId;
    

    private Integer postId;
    
    @VORelation(relationType = RelationType.MANY_TO_ONE,query = "authorId")
    private User author;
    
    private Boolean deleted = false;
    

    private String deleteReason;
    

    private LocalDateTime deleteTime;

    private Integer likeCount;

    private LocalDateTime createdAt;
    

    private Integer targetCommentId;
}