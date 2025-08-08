package com.spauter.ideasphere.service;

import com.spauter.extra.database.service.BaseService;
import com.spauter.ideasphere.entity.Post;

import java.sql.SQLException;

public interface PostService extends BaseService<Post> {

    void likePost(Object postId,Object userid)throws SQLException;

    void reportPost(Object id,String reason)throws SQLException;
}
