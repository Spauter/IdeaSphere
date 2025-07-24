-- 用户表
CREATE TABLE if not exists `user`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_uid`     int          NOT NULL COMMENT '用户UID',
    `username`     varchar(80)  NOT NULL COMMENT '用户名',
    `password`     varchar(120) NOT NULL COMMENT '密码',
    `role`         varchar(10)  DEFAULT 'user' COMMENT '角色(user/admin)',
    `icenter_user` varchar(255) DEFAULT NULL COMMENT 'iCenter用户名',
    `icenter_pwd`  varchar(255) DEFAULT NULL COMMENT 'iCenter密码',
    `created_at`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_uid` (`user_uid`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';
-- 用户关注关系表(多对多)
CREATE TABLE if not exists `user_followers`
(
    `follower_id`  bigint NOT NULL COMMENT '关注者ID',
    `following_id` bigint NOT NULL COMMENT '被关注者ID',
    PRIMARY KEY (`follower_id`, `following_id`),
    KEY `idx_following_id` (`following_id`),
    CONSTRAINT `fk_follower_user` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_following_user` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户关注关系表';
