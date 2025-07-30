-- 用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`           INTEGER PRIMARY KEY AUTOINCREMENT,
    `user_uid`     INTEGER NOT NULL UNIQUE,
    `username`     TEXT NOT NULL UNIQUE,
    `password`     TEXT NOT NULL,
    `role`         TEXT DEFAULT 'user',
    `icenter_user` TEXT,
    `icenter_pwd`  TEXT,
    `created_at`   TEXT DEFAULT CURRENT_TIMESTAMP
);

-- 用户关注关系表(多对多)
CREATE TABLE IF NOT EXISTS `user_followers`
(
    `follower_id`  INTEGER NOT NULL,
    `following_id` INTEGER NOT NULL,
    PRIMARY KEY (`follower_id`, `following_id`),
    FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`following_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);

CREATE INDEX `idx_following_id` ON `user_followers` (`following_id`);
