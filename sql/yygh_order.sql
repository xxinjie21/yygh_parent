/*
 Navicat Premium 数据传输

 源服务器         : 1
 源服务器类型    : MySQL
 源服务器版本 : 80028
 源主机           : localhost:3306
 源数据库         : yygh_order

 目标服务器类型    : MySQL
 目标服务器版本 : 80028
 文件编码         : 65001

 日期: 23/10/2022 16:22:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 表结构: order_info
-- ----------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NULL DEFAULT NULL,
  `out_trade_no` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单交易号',
  `hoscode` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '医院编号',
  `hosname` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '医院名称',
  `depcode` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '科室编号',
  `depname` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '科室名称',
  `title` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '医生职称',
  `hos_schedule_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '排班编号（医院自己的排班主键）',
  `reserve_date` date NULL DEFAULT NULL COMMENT '安排日期',
  `reserve_time` tinyint NULL DEFAULT 0 COMMENT '安排时间（0：上午 1：下午）',
  `patient_id` bigint NULL DEFAULT NULL COMMENT '就诊人id',
  `patient_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '就诊人名称',
  `patient_phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '就诊人手机',
  `hos_record_id` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '预约记录唯一标识（医院预约记录主键）',
  `number` int NULL DEFAULT NULL COMMENT '预约号序',
  `fetch_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '建议取号时间',
  `fetch_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '取号地点',
  `amount` decimal(10, 0) NULL DEFAULT NULL COMMENT '医事服务费',
  `quit_time` datetime NULL DEFAULT NULL COMMENT '退号时间',
  `order_status` tinyint NULL DEFAULT NULL COMMENT '订单状态',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_out_trade_no`(`out_trade_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_hoscode`(`hoscode` ASC) USING BTREE,
  INDEX `idx_hos_schedule_id`(`hos_schedule_id` ASC) USING BTREE,
  INDEX `idx_hos_record_id`(`hos_record_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 表数据: order_info
-- ----------------------------
INSERT INTO `order_info` VALUES (29, 11, '165935057072852', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '62e5f647637ed916179d657e', '2022-08-03', 0, 8, 'ss', '15999999999', '12', 13, '2022-08-0309:00前', '一层114窗口', 100, '2022-08-02 15:30:00', 1, '2022-08-01 18:42:50', '2022-08-01 18:42:51', 0);
INSERT INTO `order_info` VALUES (30, 11, '165935099855160', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '62e5f647637ed916179d657b', '2022-08-02', 0, 8, 'ss', '15999999999', '13', 12, '2022-08-0209:00前', '一层114窗口', 100, '2022-08-01 15:30:00', 1, '2022-08-01 18:49:58', '2022-08-01 18:49:58', 0);
INSERT INTO `order_info` VALUES (31, 11, '165935196520632', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '62e5f647637ed916179d657b', '2022-08-02', 0, 8, 'ss', '15999999999', '14', 13, '2022-08-0209:00前', '一层114窗口', 100, '2022-08-01 15:30:00', 1, '2022-08-01 19:06:05', '2022-08-01 19:06:05', 0);
INSERT INTO `order_info` VALUES (32, 11, '165935285718979', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '副主任医师', '62e5f647637ed916179d657c', '2022-08-02', 0, 10, 'chenhuan', '15997922750', '15', 35, '2022-08-0209:00前', '一层114窗口', 100, '2022-08-01 15:30:00', 1, '2022-08-01 19:20:57', '2022-08-01 19:20:57', 0);
INSERT INTO `order_info` VALUES (33, 11, '165940855361846', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '副主任医师', '62e5f648637ed916179d6580', '2022-08-03', 1, 10, 'chenhuan', '15997922750', '16', 18, '2022-08-0309:00前', '一层114窗口', 100, '2022-08-02 15:30:00', 0, '2022-08-02 10:49:13', '2022-08-02 10:49:13', 0);
INSERT INTO `order_info` VALUES (34, 11, '165940871139098', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '62e5f648637ed916179d658a', '2022-08-07', 0, 10, 'chenhuan', '15997922750', '17', 12, '2022-08-0709:00前', '一层114窗口', 100, '2022-08-06 15:30:00', 1, '2022-08-02 10:51:51', '2022-08-02 10:51:51', 0);
INSERT INTO `order_info` VALUES (35, 11, '165940956204889', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '62e5f647637ed916179d657e', '2022-08-03', 0, 10, 'chenhuan', '15997922750', '18', 14, '2022-08-0309:00前', '一层114窗口', 100, '2022-08-02 15:30:00', 0, '2022-08-02 11:06:02', '2022-08-02 11:06:02', 0);
INSERT INTO `order_info` VALUES (36, 11, '165950970448165', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '62e5f648637ed916179d6599', '2022-08-12', 0, 9, 'hh', '15988888888', '19', 12, '2022-08-1209:00前', '一层114窗口', 100, '2022-08-11 15:30:00', -1, '2022-08-03 14:55:04', '2022-08-03 14:55:04', 0);
INSERT INTO `order_info` VALUES (37, 11, '165951357976099', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '62e5f648637ed916179d6581', '2022-08-04', 0, 8, 'ss', '15999999999', '20', 12, '2022-08-0409:00前', '一层114窗口', 100, '2022-08-03 15:30:00', 0, '2022-08-03 15:59:40', '2022-08-03 15:59:40', 0);
INSERT INTO `order_info` VALUES (38, 11, '165951374401168', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '62e5f648637ed916179d658d', '2022-08-08', 0, 9, 'hh', '15988888888', '21', 12, '2022-08-0809:00前', '一层114窗口', 100, '2022-08-07 15:30:00', -1, '2022-08-03 16:02:24', '2022-08-03 16:02:24', 0);
INSERT INTO `order_info` VALUES (39, 11, '165951671166072', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '62e5f648637ed916179d6581', '2022-08-04', 0, 8, 'ss', '15999999999', '22', 13, '2022-08-0409:00前', '一层114窗口', 100, '2022-08-03 15:30:00', 1, '2022-08-03 16:51:52', '2022-08-03 16:51:52', 0);
INSERT INTO `order_info` VALUES (40, 11, '165953199507571', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '副主任医师', '62e5f648637ed916179d658c', '2022-08-07', 1, 8, 'ss', '15999999999', '23', 18, '2022-08-0709:00前', '一层114窗口', 100, '2022-08-06 15:30:00', -1, '2022-08-03 21:06:35', '2022-08-03 21:06:35', 0);
INSERT INTO `order_info` VALUES (41, 13, '166564077192856', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '6347a89e3ca7c879d7a22bbc', '2022-10-15', 0, 8, 'ss', '15999999999', '24', 12, '2022-10-1509:00前', '一层114窗口', 100, '2022-10-14 15:30:00', -1, '2022-10-13 13:59:31', '2022-10-13 13:59:32', 0);
INSERT INTO `order_info` VALUES (42, 13, '166564412702147', '1000_0', '北京协和医院', '200040878', '多发性硬化专科门诊', '医师', '6347a89e3ca7c879d7a22bbc', '2022-10-15', 0, 10, '橙子', '15997922750', '25', 13, '2022-10-1509:00前', '一层114窗口', 100, '2022-10-14 15:30:00', -1, '2022-10-13 14:55:27', '2022-10-13 14:55:27', 0);

-- ----------------------------
-- 表结构: payment_info
-- ----------------------------
DROP TABLE IF EXISTS `payment_info`;
CREATE TABLE `payment_info`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '编号',
  `out_trade_no` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '对外业务编号',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单id',
  `payment_type` tinyint(1) NULL DEFAULT NULL COMMENT '支付类型（微信 支付宝）',
  `trade_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '交易编号',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '支付金额',
  `subject` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '交易内容',
  `payment_status` tinyint NULL DEFAULT NULL COMMENT '支付状态',
  `callback_time` datetime NULL DEFAULT NULL COMMENT '回调时间',
  `callback_content` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '回调信息',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_out_trade_no`(`out_trade_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '支付信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 表数据: payment_info
-- ----------------------------
INSERT INTO `payment_info` VALUES (10, '165940855361846', 33, 2, NULL, 100.00, '2022-08-03|北京协和医院|多发性硬化专科门诊|副主任医师', 1, NULL, NULL, '2022-08-02 16:43:32', '2022-08-02 16:43:31', 0);
INSERT INTO `payment_info` VALUES (11, '165935196520632', 31, 2, '4200001582202208024999341911', 100.00, '2022-08-02|北京协和医院|多发性硬化专科门诊|医师', 2, '2022-08-02 21:40:25', '{transaction_id=4200001582202208024999341911, nonce_str=PLLRoOycHjkMp5JH, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuHGH0AyHInSaJqnAHyFvEUM, sign=7D82A5CCD4E0B86DB76A40D3AD685589, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=165935196520632, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20220802175444, is_subscribe=N, return_code=SUCCESS}', '2022-08-02 17:54:09', '2022-08-02 17:54:09', 0);
INSERT INTO `payment_info` VALUES (12, '165935057072852', 29, 2, '4200001583202208028699746853', 100.00, '2022-08-03|北京协和医院|多发性硬化专科门诊|医师', 2, '2022-08-02 21:36:09', '{transaction_id=4200001583202208028699746853, nonce_str=UUtKPpv31AXgVtc1, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuHGH0AyHInSaJqnAHyFvEUM, sign=D9569DD86A8BACBABAEFEBFD9CB12000, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=165935057072852, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20220802213131, is_subscribe=N, return_code=SUCCESS}', '2022-08-02 21:31:17', '2022-08-02 21:31:17', 0);
INSERT INTO `payment_info` VALUES (13, '165935099855160', 30, 2, '4200001477202208029424965238', 100.00, '2022-08-02|北京协和医院|多发性硬化专科门诊|医师', 2, '2022-08-02 21:36:41', '{transaction_id=4200001477202208029424965238, nonce_str=ZqdblAWyPYqN5iUZ, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuHGH0AyHInSaJqnAHyFvEUM, sign=6372B16177723CB2BEBB798618793A77, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=165935099855160, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20220802213640, is_subscribe=N, return_code=SUCCESS}', '2022-08-02 21:36:33', '2022-08-02 21:36:32', 0);
INSERT INTO `payment_info` VALUES (14, '165935285718979', 32, 2, '4200001559202208024651397387', 100.00, '2022-08-02|北京协和医院|多发性硬化专科门诊|副主任医师', 2, '2022-08-02 21:42:26', '{transaction_id=4200001559202208024651397387, nonce_str=imUkXUNwdhOBkkHr, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuHGH0AyHInSaJqnAHyFvEUM, sign=949EE6B6B2ADEE1D2C6945E17D019F00, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=165935285718979, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20220802214223, is_subscribe=N, return_code=SUCCESS}', '2022-08-02 21:41:29', '2022-08-02 21:41:28', 0);
INSERT INTO `payment_info` VALUES (15, '165940871139098', 34, 2, '4200001468202208034069385569', 100.00, '2022-08-07|北京协和医院|多发性硬化专科门诊|医师', 2, '2022-08-03 14:22:51', '{transaction_id=4200001468202208034069385569, nonce_str=3wcVGL1gvEful2Y5, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuHGH0AyHInSaJqnAHyFvEUM, sign=55FE3B80777D1FCC51DB70D5C4634E0B, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=165940871139098, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20220803142247, is_subscribe=N, return_code=SUCCESS}', '2022-08-03 14:22:23', '2022-08-03 14:22:22', 0);
INSERT INTO `payment_info` VALUES (16, '165950970448165', 36, 2, '4200001567202208037340353030', 100.00, '2022-08-12|北京协和医院|多发性硬化专科门诊|医师', 2, '2022-08-03 14:56:41', '{transaction_id=4200001567202208037340353030, nonce_str=6rxKV9CQBcds67TV, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuHGH0AyHInSaJqnAHyFvEUM, sign=FC0049CC526F9B7D61A86F817B7F42A1, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=165950970448165, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20220803145637, is_subscribe=N, return_code=SUCCESS}', '2022-08-03 14:56:31', '2022-08-03 14:56:31', 0);
INSERT INTO `payment_info` VALUES (17, '165951374401168', 38, 2, '4200001539202208039042447207', 100.00, '2022-08-08|北京协和医院|多发性硬化专科门诊|医师', 2, '2022-08-03 16:44:03', '{transaction_id=4200001539202208039042447207, nonce_str=mtqxwFgF5RXnUSZn, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuA67GeWgOE0Gz2gaIlVYi9Q, sign=13E53D7CF2030AF3CA0C478E240CFFBF, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=165951374401168, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20220803164323, is_subscribe=N, return_code=SUCCESS}', '2022-08-03 16:43:07', '2022-08-03 16:43:07', 0);
INSERT INTO `payment_info` VALUES (18, '165951671166072', 39, 2, '4200001584202208037947984925', 100.00, '2022-08-04|北京协和医院|多发性硬化专科门诊|医师', 2, '2022-08-03 21:02:00', '{transaction_id=4200001584202208037947984925, nonce_str=6gst1PFL8ANSO5A0, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuHGH0AyHInSaJqnAHyFvEUM, sign=A093EAF197220660807E9250584DEEC0, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=165951671166072, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20220803210157, is_subscribe=N, return_code=SUCCESS}', '2022-08-03 16:52:41', '2022-08-03 16:52:41', 0);
INSERT INTO `payment_info` VALUES (19, '165953199507571', 40, 2, '4200001578202208039188658924', 100.00, '2022-08-07|北京协和医院|多发性硬化专科门诊|副主任医师', 2, '2022-08-03 21:06:54', '{transaction_id=4200001578202208039188658924, nonce_str=wzbjLCPkQqYWWZ9u, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuHGH0AyHInSaJqnAHyFvEUM, sign=7D3AA7FE3BD8A994CB35C85D0563C083, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=165953199507571, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20220803210653, is_subscribe=N, return_code=SUCCESS}', '2022-08-03 21:06:42', '2022-08-03 21:06:41', 0);
INSERT INTO `payment_info` VALUES (20, '166564077192856', 41, 2, NULL, 100.00, '2022-10-15|北京协和医院|多发性硬化专科门诊|医师', 1, NULL, NULL, '2022-10-13 14:00:33', '2022-10-13 14:00:32', 0);
INSERT INTO `payment_info` VALUES (21, '166564412702147', 42, 2, '4200001563202210134810571934', 100.00, '2022-10-15|北京协和医院|多发性硬化专科门诊|医师', 2, '2022-10-13 14:57:36', '{transaction_id=4200001563202210134810571934, nonce_str=hzvzcQPX7qsBWnvL, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuHGH0AyHInSaJqnAHyFvEUM, sign=42AA74D62FBDE875F16B7D6E2ECDCADB, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=166564412702147, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20221013145735, is_subscribe=N, return_code=SUCCESS}', '2022-10-13 14:56:56', '2022-10-13 14:56:55', 0);

-- ----------------------------
-- 表结构: refund_info
-- ----------------------------
DROP TABLE IF EXISTS `refund_info`;
CREATE TABLE `refund_info`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '编号',
  `out_trade_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '对外业务编号',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单编号',
  `payment_type` tinyint NULL DEFAULT NULL COMMENT '支付类型（微信 支付宝）',
  `trade_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '交易编号',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '退款金额',
  `subject` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '交易内容',
  `refund_status` tinyint NULL DEFAULT NULL COMMENT '退款状态',
  `callback_content` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '回调信息',
  `callback_time` datetime NULL DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_out_trade_no`(`out_trade_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '退款信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 表数据: refund_info
-- ----------------------------
INSERT INTO `refund_info` VALUES (1, '165940871139098', 34, 2, NULL, 100.00, '2022-08-07|北京协和医院|多发性硬化专科门诊|医师', 1, NULL, NULL, '2022-08-03 14:23:18', '2022-08-03 14:23:18', 0);
INSERT INTO `refund_info` VALUES (2, '165950970448165', 36, 2, '50301702632022080323366699004', 100.00, '2022-08-12|北京协和医院|多发性硬化专科门诊|医师', 2, '{\"transaction_id\":\"4200001567202208037340353030\",\"nonce_str\":\"5Fh1PoJiC6N3PWkB\",\"out_refund_no\":\"tk165950970448165\",\"sign\":\"823650F79B0433924F6F18E994B20C35\",\"return_msg\":\"OK\",\"mch_id\":\"1558950191\",\"refund_id\":\"50301702632022080323366699004\",\"cash_fee\":\"1\",\"out_trade_no\":\"165950970448165\",\"coupon_refund_fee\":\"0\",\"refund_channel\":\"\",\"appid\":\"wx74862e0dfcf69954\",\"refund_fee\":\"1\",\"total_fee\":\"1\",\"result_code\":\"SUCCESS\",\"coupon_refund_count\":\"0\",\"cash_refund_fee\":\"1\",\"return_code\":\"SUCCESS\"}', '2022-08-03 18:59:01', '2022-08-03 14:56:50', '2022-08-03 14:56:49', 0);
INSERT INTO `refund_info` VALUES (3, '165951374401168', 38, 2, '50301802722022080323367870857', 100.00, '2022-08-08|北京协和医院|多发性硬化专科门诊|医师', 2, '{\"transaction_id\":\"4200001539202208039042447207\",\"nonce_str\":\"UZeFfXRib4Xwkvbq\",\"out_refund_no\":\"tk165951374401168\",\"sign\":\"D9680B094D885EBC9D9B1187B72A2267\",\"return_msg\":\"OK\",\"mch_id\":\"1558950191\",\"refund_id\":\"50301802722022080323367870857\",\"cash_fee\":\"1\",\"out_trade_no\":\"165951374401168\",\"coupon_refund_fee\":\"0\",\"refund_channel\":\"\",\"appid\":\"wx74862e0dfcf69954\",\"refund_fee\":\"1\",\"total_fee\":\"1\",\"result_code\":\"SUCCESS\",\"coupon_refund_count\":\"0\",\"cash_refund_fee\":\"1\",\"return_code\":\"SUCCESS\"}', '2022-08-03 18:56:07', '2022-08-03 16:45:33', '2022-08-03 16:45:33', 0);
INSERT INTO `refund_info` VALUES (4, '165953199507571', 40, 2, '50302002602022080323369784735', 100.00, '2022-08-07|北京协和医院|多发性硬化专科门诊|副主任医师', 2, '{\"transaction_id\":\"4200001578202208039188658924\",\"nonce_str\":\"rZOA0ASZmCaNwISp\",\"out_refund_no\":\"tk165953199507571\",\"sign\":\"1D19A3DA2BA75E24A3F93DF2A4234D2E\",\"return_msg\":\"OK\",\"mch_id\":\"1558950191\",\"refund_id\":\"50302002602022080323369784735\",\"cash_fee\":\"1\",\"out_trade_no\":\"165953199507571\",\"coupon_refund_fee\":\"0\",\"refund_channel\":\"\",\"appid\":\"wx74862e0dfcf69954\",\"refund_fee\":\"1\",\"total_fee\":\"1\",\"result_code\":\"SUCCESS\",\"coupon_refund_count\":\"0\",\"cash_refund_fee\":\"1\",\"return_code\":\"SUCCESS\"}', '2022-08-03 21:08:01', '2022-08-03 21:08:00', '2022-08-03 21:08:00', 0);
INSERT INTO `refund_info` VALUES (5, '166564412702147', 42, 2, '50300203292022101325907775326', 100.00, '2022-10-15|北京协和医院|多发性硬化专科门诊|医师', 2, '{\"transaction_id\":\"4200001563202210134810571934\",\"nonce_str\":\"1FcdV4ZdUS3C2be7\",\"out_refund_no\":\"tk166564412702147\",\"sign\":\"35457F4D2A6203266BB6A1BE196D5EF9\",\"return_msg\":\"OK\",\"mch_id\":\"1558950191\",\"refund_id\":\"50300203292022101325907775326\",\"cash_fee\":\"1\",\"out_trade_no\":\"166564412702147\",\"coupon_refund_fee\":\"0\",\"refund_channel\":\"\",\"appid\":\"wx74862e0dfcf69954\",\"refund_fee\":\"1\",\"total_fee\":\"1\",\"result_code\":\"SUCCESS\",\"coupon_refund_count\":\"0\",\"cash_refund_fee\":\"1\",\"return_code\":\"SUCCESS\"}', '2022-10-13 14:58:29', '2022-10-13 14:58:27', '2022-10-13 14:58:28', 0);

SET FOREIGN_KEY_CHECKS = 1;
