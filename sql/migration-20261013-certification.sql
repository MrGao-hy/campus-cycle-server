-- =============================================================
-- 校园循环 campus_cycle 增量升级脚本：校园认证功能
-- 适用：已按旧版 schema.sql 建库的存量环境（2026-10-13）
-- 注意：ALTER 语句不可重复执行（MySQL 8 不支持 ADD COLUMN IF NOT EXISTS）；
--       全新环境直接执行最新 schema.sql 即可，无需本脚本；
--       若已按旧版（BIGINT 时间字段）建过 t_certification，请先执行
--       DROP TABLE t_certification; 再重跑本脚本（新表无存量数据）；
--       另：全库 create_time/update_time 已改为 DATETIME(3)，存量库请重建
--       （DROP 后重跑 schema.sql + mock-data.sql），不提供 BIGINT→DATETIME 数据迁移
-- =============================================================

USE `campus_cycle`;

-- 1. 用户表：角色（后台审核权限）+ 校园认证标记（审核通过时冗余更新）
ALTER TABLE `t_user`
    ADD COLUMN `role`      VARCHAR(10) NOT NULL DEFAULT 'USER' COMMENT '角色 USER-普通用户 ADMIN-管理员（后台审核）' AFTER `contact_email`,
    ADD COLUMN `certified` TINYINT     NOT NULL DEFAULT 0 COMMENT '校园认证 0-未认证 1-已认证（审核通过时冗余更新）' AFTER `role`;

-- 2. 校园认证表（一人一条记录，驳回后可重新提交覆盖原记录；
--    时间字段使用 DATETIME(3)，由应用层自动填充）
CREATE TABLE IF NOT EXISTS `t_certification` (
    `id`                 VARCHAR(32)  NOT NULL COMMENT '主键',
    `user_id`            VARCHAR(32)  NOT NULL COMMENT '申请用户 ID',
    `real_name`          VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    `gender`             VARCHAR(10)  NOT NULL COMMENT '性别 MALE-男 FEMALE-女',
    `student_no`         VARCHAR(32)  NOT NULL COMMENT '学号',
    `student_card_image` VARCHAR(512) NOT NULL COMMENT '学生证照片 URL',
    `status`             VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING-待审核 APPROVED-已通过 REJECTED-已驳回',
    `reject_reason`      VARCHAR(200) DEFAULT NULL COMMENT '驳回原因（审核不通过时必填）',
    `reviewer_id`        VARCHAR(32)  DEFAULT NULL COMMENT '审核人（管理员）ID',
    `review_time`        DATETIME(3)  DEFAULT NULL COMMENT '审核时间',
    `deleted`            TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    `create_time`        DATETIME(3)  NOT NULL COMMENT '申请时间',
    `update_time`        DATETIME(3)  NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '校园认证';

-- 3.（可选）指定后台审核管理员：把对应用户的 role 改为 ADMIN
-- UPDATE `t_user` SET `role` = 'ADMIN' WHERE `id` = 'u_xxxxxxxx';
