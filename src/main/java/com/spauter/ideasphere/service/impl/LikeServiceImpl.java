package com.spauter.ideasphere.service.impl;

import com.spauter.extra.database.service.impl.BaseServiceImpl;
import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.ideasphere.entity.Like;
import com.spauter.ideasphere.service.LikeService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;

@Service("likeService")
public class LikeServiceImpl extends BaseServiceImpl<Like> implements LikeService {

    @Override
    public Like findLikedPost(Integer userId, Integer postId) throws SQLException {
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
        queryWrapper.addEq("user_id", userId);
        queryWrapper.addEq("post_id", postId);
        return findOne(queryWrapper);
    }

    @Override
    public int insertLike(Integer userId, Integer postId) throws SQLException {
        Like like = new Like(null,userId,postId,null,LocalDateTime.now());
        return insertOne(like);
    }
}
