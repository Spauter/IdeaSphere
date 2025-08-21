package com.spauter.ideasphere.controller;

import com.spauter.extra.baseentity.utils.VOUtil;
import com.spauter.extra.baseentity.utils.ValueUtil;
import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.ideasphere.entity.Post;
import com.spauter.ideasphere.entity.User;
import com.spauter.ideasphere.service.PostService;
import com.spauter.ideasphere.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("post")
@Slf4j
public class PostController {
    @Resource(name = "postService")
    private PostService postService;
    @Resource(name = "userService")
    private UserService userService;

    @GetMapping("/posts")
    public Map<String, Object> findAll() {
        var map = new HashMap<String, Object>();
        try {
            var posts = postService.findAll();
            map.put("code", 200);
            map.put("msg", "查询成功");
            map.put("data", posts);
        } catch (Exception e) {
            log.error(e.getMessage());
            map.put("code", 500);
            map.put("msg", e.getMessage());
        }
        return map;
    }


    @PostMapping("/createPost")
    public Map<String, Object> createPost(HttpServletRequest request, HttpSession session) {
        var map = new HashMap<String, Object>();
        var token = request.getHeader("Token");
        var user = (User) session.getAttribute(token);
        if (user == null) {
            map.put("code", 404);
            map.put("msg", "未登录");
            return map;
        }
        var userId = user.getUserUid();
        var content = request.getParameter("content");
        var title=request.getParameter("title");
        try {
            Post post=new Post();
            post.setCreatedAt(LocalDateTime.now());
            post.setContent(content);
            post.setAuthorId(ValueUtil.getIntValue(userId));
            post.setLikeCount(0);
            post.setLookCount(0);
            post.setSectionId(0);
            post.setHtmlContent(content);
            postService.insertOne(post);
            map.put("code", 200);
            map.put("msg", "添加帖子成功");
        } catch (SQLException e) {
            log.error("添加帖子失败", e);
            map.put("code", 500);
            map.put("msg", e.getMessage());
        }
        return map;
    }

    @PostMapping("/post/{postId}/like")
    public Map<String, Object> likePost() {
        Map<String, Object> map = new HashMap<>();
        return map;
    }

@PostMapping("/author")
    public Map<String, Object> findPostByAuthor(HttpServletRequest request) throws SQLException {
        var map = new HashMap<String, Object>();
        String userName = request.getParameter("username");
        String sql = "select * from user where username=?";
        try {
            User user = userService.selectOne(sql, new User(), userName);
            var queryWrapper = new QueryWrapper<Post>();
            queryWrapper.addEq("author_id", user.getUserUid());
            var posts = postService.findList(queryWrapper);
            map.put("code",200);
            map.put("data",posts);
            map.put("msg","查询成功");
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
            map.put("code",500);
            map.put("msg",e.getMessage());
        }
        return map;
    }
}
