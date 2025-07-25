package com.spauter.ideasphere.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserFollowers {
    private long follower_id;
    private long following_id;
}
