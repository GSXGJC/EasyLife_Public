-- ============================================================
-- 用户关注关系表(user_follow)
-- 语义:user_id 关注了 followed_user_id(单向)。
-- 私信名单 = 我关注的人;私信门槛 = 发送者必须已关注接收者。
-- 在 Navicat 里对 easylive 库执行即可(项目无迁移工具,表都是手工建的)。
-- ============================================================
CREATE TABLE IF NOT EXISTS easylive.user_follow (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id           VARCHAR(10)     NOT NULL COMMENT '关注者id(谁点的关注),对应 user_info.user_id',
  followed_user_id  VARCHAR(10)     NOT NULL COMMENT '被关注者id(up主),对应 user_info.user_id',
  create_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_followed (user_id, followed_user_id),
  KEY idx_followed_user_id (followed_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';
