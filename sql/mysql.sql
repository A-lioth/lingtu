-- ========== MySQL：用户与持久化会话（与 prodigal-travel 中 X-User-Id、chatId 对齐）==========
-- 建库
CREATE DATABASE IF NOT EXISTS lingtu CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE lingtu;

-- 用户表：注册 / 登录（密码仅存哈希，如 BCrypt）
CREATE TABLE IF NOT EXISTS user
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(64)  NOT NULL COMMENT '登录名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    email         VARCHAR(128) NOT NULL COMMENT '邮箱',
    nickname      VARCHAR(64)           DEFAULT NULL COMMENT '昵称',
    avatar_url    VARCHAR(512)          DEFAULT NULL COMMENT '头像 URL',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '0 禁用 1 正常',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0 否 1 是',
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_email (email)
) COMMENT '用户表';

-- 会话元数据：便于按用户列出历史会话、标题等（与 LingtuAssistantController 的 chatId 一致）
CREATE TABLE IF NOT EXISTS chat_conversation
(
    id              VARCHAR(64) PRIMARY KEY COMMENT '主键',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话 ID（UUID）',
    user_id         BIGINT      NOT NULL DEFAULT 0 COMMENT '用户标识，与请求头 X-User-Id 一致',
    title           VARCHAR(255)         DEFAULT NULL COMMENT '会话标题，可由首条消息摘要生成',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_chat_conversation_id (conversation_id),
    KEY idx_chat_conversation_user (user_id, update_time)
) COMMENT '会话表';

CREATE TABLE `spring_ai_chat_memory`
(
    `id`              bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `conversation_id` varchar(255)   DEFAULT NULL COMMENT '会话ID',
    `content`         varchar(10000) DEFAULT NULL COMMENT '内容',
    `type`            varchar(255)   DEFAULT NULL COMMENT '类型',
    `timestamp`       datetime       DEFAULT NULL COMMENT '时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4;

-- 单条消息持久化（Spring AI ChatMemory / 前端消息列表）
CREATE TABLE IF NOT EXISTS chat_message
(
    id              VARCHAR(64) PRIMARY KEY COMMENT '主键',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话 ID，对应 chat_conversation.conversation_id',
    user_id         BIGINT      NOT NULL DEFAULT 0 COMMENT '用户标识，与 X-User-Id 一致',
    role            VARCHAR(32) NOT NULL COMMENT '角色：user、assistant、system、tool 等',
    content         LONGTEXT    NOT NULL COMMENT '消息内容',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_chat_message_user_conv (user_id, conversation_id),
    KEY idx_chat_message_conv_time (conversation_id, create_time)
) COMMENT '会话消息表';

-- 插入管理员账号
INSERT INTO user (username, password_hash, email, nickname, status, deleted)
VALUES ('admin',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'admin@prodigal.com',
        '系统管理员',
        1,
        0)
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash),
                        email         = VALUES(email),
                        nickname      = VALUES(nickname),
                        status        = VALUES(status),
                        deleted       = VALUES(deleted);
