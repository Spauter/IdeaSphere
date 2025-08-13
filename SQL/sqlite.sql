-- 用户表
CREATE TABLE IF NOT EXISTS user
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_uid     INTEGER NOT NULL UNIQUE,
    username     TEXT    NOT NULL UNIQUE,
    password     TEXT    NOT NULL,
    role         TEXT      DEFAULT 'user',
    icenter_user TEXT,
    icenter_pwd  TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户关注关系表（多对多）
CREATE TABLE IF NOT EXISTS user_followers
(
    follower_id  INTEGER NOT NULL,
    following_id INTEGER NOT NULL,
    PRIMARY KEY (follower_id, following_id),
    FOREIGN KEY (follower_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES user (id) ON DELETE CASCADE
);

-- 板块表
CREATE TABLE IF NOT EXISTS section
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT NOT NULL UNIQUE,
    description   TEXT,
    icon          TEXT,
    post_count    INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0
);

-- 帖子表
CREATE TABLE IF NOT EXISTS post
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    title         TEXT    NOT NULL,
    content       TEXT    NOT NULL,
    html_content  TEXT    NOT NULL,
    author_id     INTEGER NOT NULL,
    deleted       INTEGER   DEFAULT 0, -- SQLite 用 0/1 表示布尔值
    delete_reason TEXT,
    delete_time   TEXT,                -- SQLite 没有 DATETIME 类型
    like_count    INTEGER   DEFAULT 0,
    look_count    INTEGER   DEFAULT 0,
    section_id    INTEGER NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES section (id) ON DELETE CASCADE
);

-- 评论表
CREATE TABLE IF NOT EXISTS comment
(
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    content           TEXT    NOT NULL,
    html_content      TEXT    NOT NULL,
    author_id         INTEGER NOT NULL,
    post_id           INTEGER NOT NULL,
    deleted           INTEGER   DEFAULT 0,
    delete_reason     TEXT,
    delete_time       TEXT,
    like_count        INTEGER   DEFAULT 0,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_comment_id INTEGER,
    FOREIGN KEY (author_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    FOREIGN KEY (target_comment_id) REFERENCES comment (id) ON DELETE SET NULL
);

-- 回复评论表 (简化设计，移除了冗余字段)
CREATE TABLE IF NOT EXISTS reply_comment
(
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    target_comment_id INTEGER NOT NULL,
    reply_user_id     INTEGER NOT NULL, -- 添加用户ID引用
    content           TEXT    NOT NULL, -- 统一内容字段
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    like_count        INTEGER   DEFAULT 0,
    FOREIGN KEY (target_comment_id) REFERENCES comment (id) ON DELETE CASCADE,
    FOREIGN KEY (reply_user_id) REFERENCES user (id) ON DELETE CASCADE
);

-- 举报表
CREATE TABLE IF NOT EXISTS report
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    post_id     INTEGER,
    comment_id  INTEGER,
    user_id     INTEGER NOT NULL,
    resolved_by INTEGER,
    reason      TEXT    NOT NULL,
    status      TEXT      DEFAULT 'pending',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TEXT,
    FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE SET NULL,
    FOREIGN KEY (comment_id) REFERENCES comment (id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (resolved_by) REFERENCES user (id) ON DELETE SET NULL
);

-- 点赞表 (改名为 like_record 避免关键字冲突)
CREATE TABLE IF NOT EXISTS like_record
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id    INTEGER NOT NULL,
    post_id    INTEGER,
    comment_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    FOREIGN KEY (comment_id) REFERENCES comment (id) ON DELETE CASCADE,
    CHECK (
        (post_id IS NOT NULL AND comment_id IS NULL) OR
        (post_id IS NULL AND comment_id IS NOT NULL)
        )
);

-- 用户贡献表
CREATE TABLE IF NOT EXISTS user_contribution
(
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    user_uid           INTEGER NOT NULL,
    date               TEXT    NOT NULL, -- SQLite 没有 DATE 类型
    contribution_value INTEGER NOT NULL,
    UNIQUE (user_uid, date),
    FOREIGN KEY (user_uid) REFERENCES user (user_uid) ON DELETE CASCADE
);

-- 安装状态表
CREATE TABLE IF NOT EXISTS installation_status
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    is_installed INTEGER DEFAULT 0
);

-- 搜索模型表
CREATE TABLE IF NOT EXISTS search_model
(
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    keyword TEXT NOT NULL UNIQUE
);

-- 用户关注关系表（基于UID）
CREATE TABLE IF NOT EXISTS user_follow_relation
(
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    follower_user_uid  INTEGER NOT NULL,
    following_user_uid INTEGER NOT NULL,
    follow_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_user_uid) REFERENCES user (user_uid) ON DELETE CASCADE,
    FOREIGN KEY (following_user_uid) REFERENCES user (user_uid) ON DELETE CASCADE
);

-- 用户粉丝计数表
CREATE TABLE IF NOT EXISTS user_follower_count
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_user_uid  INTEGER NOT NULL UNIQUE,
    follower_count INTEGER DEFAULT 0,
    FOREIGN KEY (user_user_uid) REFERENCES user (user_uid) ON DELETE CASCADE
);

-- 用户关注计数表
CREATE TABLE IF NOT EXISTS user_following_count
(
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_user_uid   INTEGER NOT NULL UNIQUE,
    following_count INTEGER DEFAULT 0,
    FOREIGN KEY (user_user_uid) REFERENCES user (user_uid) ON DELETE CASCADE
);