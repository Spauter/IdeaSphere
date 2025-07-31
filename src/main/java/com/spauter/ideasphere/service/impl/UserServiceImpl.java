package com.spauter.ideasphere.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.spauter.extra.baseentity.builder.TablePkGenerator;
import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.database.service.impl.BaseServiceImpl;
import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.ideasphere.entity.User;
import com.spauter.ideasphere.entity.UserFollowers;
import com.spauter.ideasphere.service.UserFollowsService;
import com.spauter.ideasphere.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service("userService")
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {

    @Resource(name = "userFollowsService")
    private UserFollowsService followerService;

    @Override
    public User login(String username, String password) throws SQLException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.addEq("username", username);
        String pwd = DigestUtil.sha256Hex(password);
        queryWrapper.addEq("password", pwd);
        return findOne(queryWrapper);
    }

    //获取所有关注人员
    @Override
    public List<User> getFollowing(long userId) throws SQLException {
        QueryWrapper<UserFollowers> queryWrapper = new QueryWrapper<>();
        queryWrapper.addEq("follower_id", userId);
        List<UserFollowers> followers = followerService.findList(queryWrapper);
        List<User> users = new ArrayList<>();
        for (UserFollowers f : followers) {
            User user = findById(f.getFollowing_id());
            users.add(user);
        }
        return users;
    }

    @Override
    public List<User> getFollowers(long userId) throws SQLException {
        QueryWrapper<UserFollowers> queryWrapper = new QueryWrapper<>();
        queryWrapper.addEq("following_id", userId);
        List<UserFollowers> followers = followerService.findList(queryWrapper);
        List<User> users = new ArrayList<>();
        for (UserFollowers f : followers) {
            User user = findById(f.getFollower_id());
            users.add(user);
        }
        return users;
    }

    @Override
    public User register(String userName, String password) throws SQLException {
        User user = new User();
        int id = Math.toIntExact(TablePkGenerator.generateIdByAutoIncrement(new ClassFieldSearcher(user.getClass())));
        user.setId((long) id);
        user.setUserUid(id);
        user.setUsername(userName);
        user.setPassword(DigestUtil.sha256Hex(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole("user");
        user.setIcenterUser(userName);
        user.setIcenterPwd(password);
        insertOne(user);
        return user;
    }
}
