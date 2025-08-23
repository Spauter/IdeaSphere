package com.spauter.ideasphere.service;

import com.spauter.extra.database.service.BaseService;
import com.spauter.ideasphere.entity.Like;

import java.sql.SQLException;

public interface LikeService extends BaseService<Like> {
    Like findLikedPost(Integer userId, Integer postId) throws SQLException;

    int insertLike(Integer userId, Integer postId) throws SQLException;
}
