-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_uid` INT NOT NULL UNIQUE,
  `username` VARCHAR(80) NOT NULL UNIQUE,
  `password` VARCHAR(120) NOT NULL,
  `role` VARCHAR(10) DEFAULT 'user',
  `icenter_user` VARCHAR(255),
  `icenter_pwd` VARCHAR(255),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户关注关系表（多对多）
CREATE TABLE IF NOT EXISTS `user_followers` (
  `follower_id` INT NOT NULL,
  `following_id` INT NOT NULL,
  PRIMARY KEY (`follower_id`, `following_id`),
  FOREIGN KEY (`follower_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`following_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 板块表
CREATE TABLE IF NOT EXISTS `section` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  `description` TEXT,
  `icon` VARCHAR(50),
  `post_count` INT DEFAULT 0,
  `comment_count` INT DEFAULT 0
);

-- 帖子表
CREATE TABLE IF NOT EXISTS `post` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `html_content` TEXT NOT NULL,
  `author_id` INT NOT NULL,
  `deleted` BOOLEAN DEFAULT FALSE,
  `delete_reason` TEXT,
  `delete_time` DATETIME,
  `like_count` INT DEFAULT 0,
  `look_count` INT DEFAULT 0,
  `section_id` INT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`author_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`section_id`) REFERENCES `section`(`id`) ON DELETE CASCADE
);

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `content` TEXT NOT NULL,
  `html_content` TEXT NOT NULL,
  `author_id` INT NOT NULL,
  `post_id` INT NOT NULL,
  `deleted` BOOLEAN DEFAULT FALSE,
  `delete_reason` TEXT,
  `delete_time` DATETIME,
  `like_count` INT DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `target_comment_id` INT,
  FOREIGN KEY (`author_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`post_id`) REFERENCES `post`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`target_comment_id`) REFERENCES `comment`(`id`) ON DELETE SET NULL
);

-- 回复评论表
CREATE TABLE IF NOT EXISTS `reply_comment` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `reply_message` TEXT NOT NULL,
  `reply_user` TEXT NOT NULL,
  `target_comment_id` INT NOT NULL,
  `reply_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `like_count` INT DEFAULT 0,
  FOREIGN KEY (`target_comment_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE
);

-- 举报表
CREATE TABLE IF NOT EXISTS `report` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `post_id` INT,
  `comment_id` INT,
  `user_id` INT NOT NULL,
  `resolved_by` INT,
  `reason` TEXT NOT NULL,
  `status` VARCHAR(20) DEFAULT 'pending',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` DATETIME,
  FOREIGN KEY (`post_id`) REFERENCES `post`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`comment_id`) REFERENCES `comment`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`resolved_by`) REFERENCES `user`(`id`) ON DELETE SET NULL
);

-- 点赞表
CREATE TABLE IF NOT EXISTS `like` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `post_id` INT,
  `comment_id` INT,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`post_id`) REFERENCES `post`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`comment_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE,
  CONSTRAINT `check_like_target` CHECK (
    (post_id IS NOT NULL AND comment_id IS NULL) OR
    (post_id IS NULL AND comment_id IS NOT NULL)
));

-- 用户贡献表
CREATE TABLE IF NOT EXISTS `user_contribution` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_uid` INT NOT NULL,
  `date` DATE NOT NULL,
  `contribution_value` INT NOT NULL,
  UNIQUE KEY `_user_uid_date_uc` (`user_uid`, `date`),
  FOREIGN KEY (`user_uid`) REFERENCES `user`(`user_uid`) ON DELETE CASCADE
);

-- 安装状态表
CREATE TABLE IF NOT EXISTS `installation_status` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `is_installed` BOOLEAN DEFAULT FALSE
);

-- 搜索模型表
CREATE TABLE IF NOT EXISTS `search_model` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `keyword` VARCHAR(100) NOT NULL UNIQUE
);

-- 用户关注关系表（基于UID）
CREATE TABLE IF NOT EXISTS `user_follow_relation` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `follower_user_uid` INT NOT NULL,
  `following_user_uid` INT NOT NULL,
  `follow_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`follower_user_uid`) REFERENCES `user`(`user_uid`) ON DELETE CASCADE,
  FOREIGN KEY (`following_user_uid`) REFERENCES `user`(`user_uid`) ON DELETE CASCADE
);

-- 用户粉丝计数表
CREATE TABLE IF NOT EXISTS `user_follower_count` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_user_uid` INT NOT NULL UNIQUE,
  `follower_count` INT DEFAULT 0,
  FOREIGN KEY (`user_user_uid`) REFERENCES `user`(`user_uid`) ON DELETE CASCADE
);

-- 用户关注计数表
CREATE TABLE IF NOT EXISTS `user_following_count` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_user_uid` INT NOT NULL UNIQUE,
  `following_count` INT DEFAULT 0,
  FOREIGN KEY (`user_user_uid`) REFERENCES `user`(`user_uid`) ON DELETE CASCADE
);