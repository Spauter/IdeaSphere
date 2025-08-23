package com.spauter.ideasphere.entity;

import com.spauter.extra.baseentity.enums.IdType;
import com.spauter.extra.database.annotations.TableId;
import com.spauter.extra.database.annotations.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName(value = "likes")
@AllArgsConstructor
@NoArgsConstructor
public class Like {
    @TableId(idType = IdType.AUTO_INCREMENT)
    private Integer id;
    

    private Integer userId;
    

    private Integer postId;
    

    private Integer commentId;

    private LocalDateTime createdAt;
}