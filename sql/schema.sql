-- =============================================================
-- 校园循环 campus_cycle 数据库初始化脚本
-- 适用：MySQL 8.x
-- 说明：业务时间戳统一使用 BIGINT 存毫秒，与前端 number 类型契约一致
-- =============================================================

CREATE DATABASE IF NOT EXISTS `campus_cycle` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `campus_cycle`;

-- -------------------------------------------------------------
-- 1. 学校
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_school`;
CREATE TABLE `t_school` (
    `id`           VARCHAR(32)  NOT NULL COMMENT '主键',
    `name`         VARCHAR(50)  NOT NULL COMMENT '学校全称',
    `short_name`   VARCHAR(20)  NOT NULL COMMENT '学校简称',
    `goods_count`  INT          NOT NULL DEFAULT 0 COMMENT '在售商品数（冗余统计）',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间(ms)',
    `update_time`  BIGINT       NOT NULL COMMENT '更新时间(ms)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学校';

-- -------------------------------------------------------------
-- 2. 用户
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id`             VARCHAR(32)  NOT NULL COMMENT '主键',
    `openid`         VARCHAR(64)  DEFAULT NULL COMMENT '微信 openid（mock 模式可为空）',
    `nickname`       VARCHAR(50)  NOT NULL COMMENT '昵称',
    `avatar`         VARCHAR(512) DEFAULT NULL COMMENT '头像 URL',
    `school_id`      VARCHAR(32)  NOT NULL COMMENT '所属学校',
    `credit_score`   INT          NOT NULL DEFAULT 100 COMMENT '信用分',
    `success_count`  INT          NOT NULL DEFAULT 0 COMMENT '成功交易笔数（首单免手续费依据）',
    `contact_phone`  VARCHAR(20)  DEFAULT NULL COMMENT '联系方式-电话',
    `contact_qq`     VARCHAR(32)  DEFAULT NULL COMMENT '联系方式-QQ',
    `contact_wechat` VARCHAR(50)  DEFAULT NULL COMMENT '联系方式-微信',
    `contact_email`  VARCHAR(100) DEFAULT NULL COMMENT '联系方式-邮箱',
    `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    `create_time`    BIGINT       NOT NULL COMMENT '创建时间(ms)',
    `update_time`    BIGINT       NOT NULL COMMENT '更新时间(ms)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_school` (`school_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户';

-- -------------------------------------------------------------
-- 3. 商品
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_goods`;
CREATE TABLE `t_goods` (
    `id`             VARCHAR(32)   NOT NULL COMMENT '主键',
    `seller_id`      VARCHAR(32)   NOT NULL COMMENT '卖家 ID',
    `school_id`      VARCHAR(32)   NOT NULL COMMENT '所属学校',
    `title`          VARCHAR(100)  NOT NULL COMMENT '标题',
    `price`          DECIMAL(10,2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `images`         JSON          DEFAULT NULL COMMENT '图片 URL 数组',
    `category`       VARCHAR(20)   NOT NULL COMMENT '分类',
    `condition`      VARCHAR(20)   NOT NULL COMMENT '成色',
    `description`    TEXT          NOT NULL COMMENT '描述',
    `status`         VARCHAR(20)   NOT NULL DEFAULT 'ON_SALE' COMMENT '状态 ON_SALE/LOCKED/SOLD',
    `views`          INT           NOT NULL DEFAULT 0 COMMENT '浏览量',
    `want_count`     INT           NOT NULL DEFAULT 0 COMMENT '想要/申请数',
    `publish_time`   BIGINT        NOT NULL COMMENT '发布时间(ms)',
    `deleted`        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    `create_time`    BIGINT        NOT NULL COMMENT '创建时间(ms)',
    `update_time`    BIGINT        NOT NULL COMMENT '更新时间(ms)',
    PRIMARY KEY (`id`),
    KEY `idx_seller` (`seller_id`),
    KEY `idx_school_status` (`school_id`, `status`),
    KEY `idx_category` (`category`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品';

-- -------------------------------------------------------------
-- 4. 商品评价
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_review`;
CREATE TABLE `t_review` (
    `id`            VARCHAR(32)  NOT NULL COMMENT '主键',
    `goods_id`      VARCHAR(32)  NOT NULL COMMENT '商品 ID',
    `order_id`      VARCHAR(32)  NOT NULL COMMENT '订单 ID',
    `from_user_id`  VARCHAR(32)  NOT NULL COMMENT '评价人 ID',
    `from_nickname` VARCHAR(50)  NOT NULL COMMENT '评价人昵称（冗余快照）',
    `rate`          TINYINT      NOT NULL COMMENT '评分 1-5',
    `content`       VARCHAR(500) NOT NULL COMMENT '评价内容',
    `time`          BIGINT       NOT NULL COMMENT '评价时间(ms)',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    `create_time`   BIGINT       NOT NULL COMMENT '创建时间(ms)',
    `update_time`   BIGINT       NOT NULL COMMENT '更新时间(ms)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order` (`order_id`),
    KEY `idx_goods` (`goods_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品评价';

-- -------------------------------------------------------------
-- 5. 订单（order 为保留字，表名用 t_order）
-- 状态机：PENDING_SELLER → PENDING_OFFLINE → PENDING_BUYER → APPEALING → COMPLETED / CANCELLED
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
    `id`                   VARCHAR(32)   NOT NULL COMMENT '主键',
    `goods_id`             VARCHAR(32)   NOT NULL COMMENT '商品 ID',
    `buyer_id`             VARCHAR(32)   NOT NULL COMMENT '买家 ID',
    `seller_id`            VARCHAR(32)   NOT NULL COMMENT '卖家 ID',
    `status`               VARCHAR(20)   NOT NULL COMMENT '订单状态',
    `price`                DECIMAL(10,2) NOT NULL COMMENT '成交价（申请时锁定）',
    `apply_time`           BIGINT        NOT NULL COMMENT '买家申请时间(ms)',
    `expire_time`          BIGINT        NOT NULL COMMENT '卖家处理截止(ms)，申请后 24h 自动过期不收手续费',
    `seller_confirm_time`  BIGINT        DEFAULT NULL COMMENT '卖家确认时间(ms)',
    `buyer_confirm_time`   BIGINT        DEFAULT NULL COMMENT '买家确认时间(ms)',
    `appeal_end_time`      BIGINT        DEFAULT NULL COMMENT '申诉期截止(ms) = 买家确认 + 48h',
    `seller_objection`     TINYINT       NOT NULL DEFAULT 0 COMMENT '申诉期内卖家是否提出异议 0-否 1-是',
    `cancel_time`          BIGINT        DEFAULT NULL COMMENT '取消时间(ms)',
    `cancel_reason`        VARCHAR(200)  DEFAULT NULL COMMENT '取消原因',
    `complete_time`        BIGINT        DEFAULT NULL COMMENT '完成时间(ms)',
    `fee_billed`           TINYINT       NOT NULL DEFAULT 0 COMMENT '手续费是否已生成 0-未生成 1-已生成',
    `remark`               VARCHAR(500)  DEFAULT NULL COMMENT '买家申请留言',
    `deleted`              TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    `create_time`          BIGINT        NOT NULL COMMENT '创建时间(ms)',
    `update_time`          BIGINT        NOT NULL COMMENT '更新时间(ms)',
    PRIMARY KEY (`id`),
    KEY `idx_goods` (`goods_id`),
    KEY `idx_buyer_status` (`buyer_id`, `status`),
    KEY `idx_seller_status` (`seller_id`, `status`),
    KEY `idx_status_expire` (`status`, `expire_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单';

-- -------------------------------------------------------------
-- 6. 会话
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_conversation`;
CREATE TABLE `t_conversation` (
    `id`           VARCHAR(32)  NOT NULL COMMENT '主键',
    `goods_id`     VARCHAR(32)  NOT NULL COMMENT '商品 ID',
    `buyer_id`     VARCHAR(32)  NOT NULL COMMENT '买家 ID',
    `seller_id`    VARCHAR(32)  NOT NULL COMMENT '卖家 ID',
    `last_message` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '最后一条消息',
    `last_time`    BIGINT       NOT NULL COMMENT '最后消息时间(ms)',
    `unread_for`   JSON         DEFAULT NULL COMMENT '各自维度未读数 {"userId": count}',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间(ms)',
    `update_time`  BIGINT       NOT NULL COMMENT '更新时间(ms)',
    PRIMARY KEY (`id`),
    KEY `idx_goods` (`goods_id`),
    KEY `idx_buyer` (`buyer_id`),
    KEY `idx_seller` (`seller_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话';

-- -------------------------------------------------------------
-- 7. 聊天消息
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_message`;
CREATE TABLE `t_message` (
    `id`              VARCHAR(32)   NOT NULL COMMENT '主键',
    `conversation_id` VARCHAR(32)   NOT NULL COMMENT '会话 ID',
    `from_user_id`    VARCHAR(32)   NOT NULL COMMENT '发送人 ID',
    `content`         VARCHAR(1000) NOT NULL COMMENT '消息内容',
    `time`            BIGINT        NOT NULL COMMENT '发送时间(ms)',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    `create_time`     BIGINT        NOT NULL COMMENT '创建时间(ms)',
    `update_time`     BIGINT        NOT NULL COMMENT '更新时间(ms)',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_time` (`conversation_id`, `time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天消息';

-- -------------------------------------------------------------
-- 8. 手续费账单
-- 规则：首单免费；成交价 6%，最低 1 元，最高 20 元
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_fee_bill`;
CREATE TABLE `t_fee_bill` (
    `id`           VARCHAR(32)   NOT NULL COMMENT '主键',
    `order_id`     VARCHAR(32)   NOT NULL COMMENT '订单 ID（一单一账）',
    `seller_id`    VARCHAR(32)   NOT NULL COMMENT '卖家 ID',
    `goods_title`  VARCHAR(100)  NOT NULL COMMENT '商品标题快照',
    `deal_price`   DECIMAL(10,2) NOT NULL COMMENT '成交价',
    `rate`         DECIMAL(4,2)  NOT NULL COMMENT '费率 0.06',
    `amount`       DECIMAL(10,2) NOT NULL COMMENT '应收金额（首单为 0）',
    `free_reason`  VARCHAR(100)  DEFAULT NULL COMMENT '免手续费原因（如：首单免费）',
    `status`       VARCHAR(10)   NOT NULL DEFAULT 'UNPAID' COMMENT '状态 UNPAID/PAID',
    `create_time`  BIGINT        NOT NULL COMMENT '账单生成时间(ms)',
    `pay_time`     BIGINT        DEFAULT NULL COMMENT '支付时间(ms)',
    `deleted`      TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    `update_time`  BIGINT        NOT NULL COMMENT '更新时间(ms)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order` (`order_id`),
    KEY `idx_seller_status` (`seller_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '手续费账单';

-- =============================================================
-- 种子数据：学校列表（与前端 mock 对齐）
-- =============================================================
INSERT INTO `t_school` (`id`, `name`, `short_name`, `goods_count`, `deleted`, `create_time`, `update_time`) VALUES
('s_0001', '华中科技大学', '华科', 0, 0, 0, 0),
('s_0002', '武汉大学', '武大', 0, 0, 0, 0),
('s_0003', '武汉理工大学', '武理', 0, 0, 0, 0),
('s_0004', '华中师范大学', '华师', 0, 0, 0, 0),
('s_0005', '中国地质大学（武汉）', '地大', 0, 0, 0, 0);
