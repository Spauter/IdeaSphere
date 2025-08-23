package com.spauter.ideasphere.controller;

import com.spauter.extra.baseentity.utils.VOUtil;
import com.spauter.extra.baseentity.utils.ValueUtil;
import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.ideasphere.entity.Post;
import com.spauter.ideasphere.entity.User;
import com.spauter.ideasphere.service.LikeService;
import com.spauter.ideasphere.service.PostService;
import com.spauter.ideasphere.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spauter.extra.baseentity.utils.ValueUtil.getIntValue;


@RestController
@RequestMapping("post")
@Slf4j
public class PostController {
    @Resource(name = "postService")
    private PostService postService;
    @Resource(name = "userService")
    private UserService userService;
    @Resource(name="likeService")
    private LikeService likeService;
    @Resource(name = "customRedisTemplate")
    private RedisTemplate<String,Object>redisTemplate;

    @GetMapping(value = "/posts", name = "获取所有帖子的API")
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


    @PostMapping(value = "/createPost",name="创建帖子的api")
    //todo
    public Map<String, Object> createPost(HttpServletRequest request, HttpSession session) {
        var map = new HashMap<String, Object>();
        var token = request.getHeader("Token");
        var user =(User) redisTemplate.opsForValue().get(token);
        if (user == null) {
            map.put("code", 401);
            map.put("msg", "未登录");
            return map;
        }
        var userId = user.getUserUid();
        var content = request.getParameter("content");
        var title=request.getParameter("title");
        try {
            Post post=new Post();
            post.setCreatedAt(LocalDateTime.now());
            post.setHtmlContent(title);
            post.setContent(content);
            post.setAuthorId(getIntValue(userId));
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

    @PostMapping("/{post_id}/like")
    public Map<String, Object> likePost(HttpServletRequest request) throws SQLException {
        var token = request.getHeader("Token");
        var user = (User) redisTemplate.opsForValue().get(token);
        if (user == null) {
            var map = new HashMap<String, Object>();
            map.put("code", 401);
            map.put("msg", "未登录");
            return map;
        }
        String post_id = request.getParameter("post_id");
        var map = new HashMap<String,Object>();
        var post=postService.findById(post_id);
        var like=likeService.findLikedPost(user.getUserUid(),getIntValue(post_id));
        if(like!=null){
            map.put("code",400);
            map.put("msg","已经点赞过了");
            return map;
        }
        post.setLikeCount(post.getLikeCount()+1);
        postService.updateById(post);
        likeService.insertLike(user.getUserUid(),getIntValue(post_id));
        map.put("code",200);
        map.put("msg","点赞成功");
        return map;
    }


    @GetMapping(value = "/{post_id}", name = "获取单个帖子的API")
    public Post getPostById(@PathVariable Object post_id) throws SQLException {
        return postService.findById(post_id);
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
            map.put("code", 200);
            map.put("data", posts);
            map.put("msg", "查询成功");
        } catch (Exception e) {
            log.error(e.getMessage());
            map.put("code", 500);
            map.put("msg", e.getMessage());
        }
        return map;
    }
   @PostMapping("/{post_id}/comment")
    public Map<String,Object>commentPost(HttpServletRequest request){
        String token=request.getHeader("Token");
        User user=(User)redisTemplate.opsForValue().get(token);
        if(user==null){
            var map=new HashMap<String,Object>();
            map.put("code",401);
            map.put("msg","未登录");
            return map;
        }
        String post_id=request.getParameter("post_id");
        String content=request.getParameter("content");
        var map=new HashMap<String,Object>();
//        try {
       return map;
    }

}
