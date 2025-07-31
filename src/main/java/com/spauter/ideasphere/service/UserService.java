package com.spauter.ideasphere.service;

import com.spauter.extra.database.service.BaseService;
import com.spauter.ideasphere.entity.User;

import java.sql.SQLException;
import java.util.List;
public interface UserService extends BaseService<User> {
    User login(String username, String password) throws SQLException;

    List<User>getFollowing(long userId) throws SQLException;

    List<User>getFollowers(long userId) throws SQLException;

    User register(String userName,String password) throws SQLException;
}
