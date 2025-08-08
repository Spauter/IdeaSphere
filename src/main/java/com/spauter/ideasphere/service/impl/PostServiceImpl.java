package com.spauter.ideasphere.service.impl;

import com.spauter.extra.baseentity.utils.ValueUtil;
import com.spauter.extra.database.service.impl.BaseServiceImpl;
import com.spauter.ideasphere.entity.Post;
import com.spauter.ideasphere.service.PostService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;

@Service("postService")
public class PostServiceImpl extends BaseServiceImpl<Post> implements PostService {

    @Override
    public void likePost(Object postId,Object userId) throws SQLException {
        Post post=findById(postId);
        int likeCount=post.getLikeCount();
        likeCount++;
        post.setLikeCount(likeCount);
        updateById(post);
    }

    @Override
    public void reportPost(Object id, String reason) throws SQLException {

    }
}
