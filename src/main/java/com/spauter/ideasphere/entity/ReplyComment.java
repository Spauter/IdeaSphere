package com.spauter.ideasphere.entity;

import com.spauter.extra.baseentity.enums.IdType;
import com.spauter.extra.database.annotations.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyComment {

    @TableId(idType = IdType.AUTO_INCREMENT)
    private Integer id;
    

    private String replyMessage;
    

    private String replyUser;
    

    private Integer targetCommentId;
    

    private LocalDateTime replyAt;
    

    private Integer likeCount;
}
