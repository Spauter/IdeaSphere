package com.spauter.ideasphere.entity;

import com.spauter.extra.baseentity.annotation.TableFiled;
import com.spauter.extra.baseentity.annotation.TableId;
import com.spauter.extra.baseentity.enums.IdType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class User {
    @TableId(idType= IdType.AUTO_INCREMENT)
    private Long id;
    private Integer userUid;
    private String username;
    private String password;
    private String role;
    private String icenterUser;
    private String icenterPwd;
    private LocalDateTime createdAt;
    @TableFiled(exists = false)
    private List<User> following;
    @TableFiled(exists = false)
    private List<User> followers;
}
