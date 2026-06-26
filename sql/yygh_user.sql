/*
 Navicat Premium 数据传输

 源服务器         : 1
 源服务器类型    : MySQL
 源服务器版本 : 80028
 源主机           : localhost:3306
 源数据库         : yygh_user

 目标服务器类型    : MySQL
 目标服务器版本 : 80028
 文件编码         : 65001

 日期: 23/10/2022 16:22:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 表结构: patient
-- ----------------------------
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '姓名',
  `certificates_type` varchar(3) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '证件类型',
  `certificates_no` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '证件编号',
  `sex` tinyint NULL DEFAULT NULL COMMENT '性别',
  `birthdate` date NULL DEFAULT NULL COMMENT '出生年月',
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机',
  `is_marry` tinyint NULL DEFAULT NULL COMMENT '是否结婚',
  `province_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '省code',
  `city_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '市code',
  `district_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '区code',
  `address` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '详情地址',
  `contacts_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contacts_certificates_type` varchar(3) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人证件类型',
  `contacts_certificates_no` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人证件号',
  `contacts_phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人手机',
  `card_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '就诊卡号',
  `is_insure` tinyint NULL DEFAULT 0 COMMENT '是否有医保',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0：默认 1：已认证）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '就诊人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 表数据: patient
-- ----------------------------
INSERT INTO `patient` VALUES (8, 13, 'ss', '10', '676777666677776666', 1, '1995-04-16', '15999999999', 0, '420000', '420100', '420117', '123456789', 'cc', '10', '111222111122221121', '15997922750', NULL, 0, 0, '2022-07-29 19:57:13', '2022-08-12 10:35:33', 0);
INSERT INTO `patient` VALUES (9, 13, 'hh', '20', '22233344455556666', 0, '2000-01-05', '15988888888', 0, '420000', '421200', '421222', '123456', 'cc', '10', '111222111122221121', '15997922750', NULL, 1, 0, '2022-07-29 20:03:29', '2022-08-12 10:35:35', 0);
INSERT INTO `patient` VALUES (10, 13, '橙子', '10', '111222111122221121', 0, '2000-01-05', '15997922750', 0, '420000', '420900', '420902', '湖北工程学院', '', '', '', '', NULL, 1, 0, '2022-07-31 12:06:33', '2022-08-12 10:35:40', 0);

-- ----------------------------
-- 表结构: user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `openid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '微信openid',
  `nick_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '手机号',
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `certificates_type` varchar(3) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '证件类型',
  `certificates_no` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '证件编号',
  `certificates_url` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '证件路径',
  `auth_status` tinyint NOT NULL DEFAULT 0 COMMENT '认证状态（0：未认证 1：认证中 2：认证成功 -1：认证失败）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0：锁定 1：正常）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `uk_mobile`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 表数据: user_info
-- ----------------------------
INSERT INTO `user_info` VALUES (8, NULL, NULL, '15588889999', 'yiyi', '身份证', '4545454554545454545', 'https://c-yygh.oss-cn-beijing.aliyuncs.com/2022/07/29/2fb8a8c3718f47a094670c3132a954a67.jpg', -1, 1, '2022-07-23 10:50:35', '2022-07-29 17:30:15', 0);
INSERT INTO `user_info` VALUES (12, 'o3_SC57PmEG6NGpVS-M9PTq4ljGI', '书迟', '13886378533', '猫猫一号', '身份证', '111111111111111111', 'https://c-yygh.oss-cn-beijing.aliyuncs.com/2022/07/28/f91456c4d503425b8be7314e484241693.png', 1, 1, '2022-07-28 21:25:09', '2022-07-29 17:30:10', 0);
INSERT INTO `user_info` VALUES (13, 'o3_SC51ZU7_hc3IpvsAmFuWsX3K0', '西南', '15997922750', 'cc', '身份证', '121212121212121212', 'https://c-yygh.oss-cn-beijing.aliyuncs.com/2022/08/12/2c5473c91c38484d9a5aac4ec1a34e4cbdd.jpg', 2, 1, '2022-08-12 10:18:46', '2022-08-12 10:18:46', 0);

-- ----------------------------
-- 表结构: user_login_record
-- ----------------------------
DROP TABLE IF EXISTS `user_login_record`;
CREATE TABLE `user_login_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
  `ip` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'ip',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户登录记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 表数据: user_login_record
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
